package dobby;

import dobby.filter.FilterDiscoverer;
import dobby.filter.FilterManager;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.routes.RouteDiscoverer;
import dobby.session.Session;
import dobby.util.Config;
import dobby.util.logging.LogLevel;
import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The Server class is used to start the server
 */
public class Dobby {
    private static final String version = "0.0.1";
    private static Class<?> applicationClass;
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

        if (!Config.getInstance().getBoolean("dobby.disableFilters")) {
            LOGGER.info("Discovering filters...");
            discoverFilterDefinitions();
        }
        LOGGER.info("done!");
        start();
    }

    public static void startApplication(Class<?> applicationClass) {
        Dobby.applicationClass = applicationClass;
        printBanner();
        Config config = Config.getInstance();
        config.loadConfig();

        System.out.println("[" + config.getString("application.name", "[APP_NAME]") + "@" + config.getString(
                "application.version", "[APP_VERSION]") + "]");
        System.out.println();

        setLogLevel(config.getString("dobby.logLevel", "DEBUG"));

        new Dobby(config.getInt("dobby.port", 3000), config.getInt("dobby.threads", 10));
    }

    private static void setLogLevel(String logLevelString) {
        LogLevel logLevel = LogLevel.DEBUG;
        try {
            logLevel = LogLevel.valueOf(logLevelString.toUpperCase());
        } catch (IllegalArgumentException e) {
            new Logger(Dobby.class).warn("invalid log level: " + logLevelString + ", using DEBUG");
        }
        Logger.setMaxLogLevel(logLevel);
    }

    public static Class<?> getMainClass() {
        return applicationClass;
    }

    private static void printBanner() {
        System.out.println("########   #######  ########  ########  ##    ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##  ##  ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##   ####");
        System.out.println("##     ## ##     ## ########  ########     ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##    ##");
        System.out.println("##     ## ##     ## ##     ## ##     ##    ##");
        System.out.println("########   #######  ########  ########     ##");
        System.out.println("v" + version);
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

        ctx.setRequest(req);
        ctx.setResponse(res);
        ctx.setSession(new Session()); // if available, session will be set in SessionPreFilter

        FilterManager.getInstance().runFilterChain(ctx);
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
