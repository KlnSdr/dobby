package dobby;

import dobby.filter.FilterDiscoverer;
import dobby.filter.FilterManager;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.routes.RouteDiscoverer;
import dobby.routes.RouteManager;
import dobby.session.Session;
import dobby.session.service.SessionService;
import dobby.util.ConfigFile;
import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The Server class is used to start the server
 */
public class Dobby {
    private final Logger LOGGER = new Logger(Dobby.class);
    private final Date startTime;
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean isRunning = false;

    private Dobby(int port, int threadCount) {
        startTime = new Date();
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            LOGGER.trace(e);
            System.exit(1);
            return;
        }
        threadPool = Executors.newFixedThreadPool(threadCount);
        LOGGER.info("Server initialized on port " + port + " with " + threadCount + " threads.");
        LOGGER.info("Discovering routes...");
        discoverRouteDefinitions();
        LOGGER.info("done!");
        LOGGER.info("Discovering filters...");
        discoverFilterDefinitions();
        LOGGER.info("done!");
        start();
    }

    public static void startApplication(Class<?> applicationClass) {
        printBanner();
        ConfigFile config = new ConfigFile(applicationClass);
        new Dobby(config.getPort(), config.getThreads());
    }

    private static void printBanner() {
        System.out.println("########   #######  ########  ########  ##    ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##  ##  ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##   ####");
        System.out.println("##     ## ##     ## ########  ########     ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##    ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##    ##");
        System.out.println("########   #######  ########  ########     ##");
        System.out.println("initializing...");
        System.out.println();
    }

    private void start() {
        LOGGER.info("ready after " + (new Date().getTime() - startTime.getTime()) + "ms");
        LOGGER.info("Server started...");
        isRunning = true;
        registerStopHandler();
        acceptConnections();
    }

    private void discoverRouteDefinitions() {
        RouteDiscoverer.discoverRoutes("");
    }

    private void discoverFilterDefinitions() {
        FilterDiscoverer.discover("");
    }

    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket client = server.accept();
                threadPool.execute(() -> {
                    try {
                        handleConnection(client);
                    } catch (IOException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException e) {
                        LOGGER.trace(e);
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            LOGGER.trace(e);
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.trace(e);
            }
        }
    }

    private void handleConnection(Socket client) throws IOException, InvocationTargetException, NoSuchMethodException
            , InstantiationException, IllegalAccessException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        HttpContext ctx = new HttpContext();

        Request req = Request.parse(in);
        Response res = new Response(client, ctx);
        Optional<Session> session = SessionService.getInstance().find(req.getCookie("DOBBY_SESSION"));

        ctx.setRequest(req);
        ctx.setResponse(res);
        ctx.setSession(session.orElse(new Session()));

        FilterManager.getInstance().runPreFilters(ctx);
        RouteManager.getInstance().getHandler(req.getType(), req.getPath()).handle(ctx);
    }

    /**
     * Stops the server
     */
    private void stop() {
        isRunning = false;
        LOGGER.info("Server stopping...");
        threadPool.shutdown();
        try {
            if (threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.info("all tasks finished");
            } else {
                LOGGER.info("forcing shutdown");
                threadPool.shutdownNow();
            }
            server.close();
            LOGGER.info("Server stopped.");
        } catch (IOException | InterruptedException e) {
            LOGGER.trace(e);
        }
    }

    private void registerStopHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
}
