package dobby;

import dobby.filter.FilterDiscoverer;
import dobby.filter.FilterManager;
import dobby.filter.post.PostFilter;
import dobby.filter.pre.PreFilter;
import dobby.logging.Logger;
import dobby.routes.RouteDiscoverer;
import dobby.routes.RouteManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final Logger LOGGER = new Logger(Server.class);
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    private final Date startTime;

    private Server(int port, int threadCount) {
        startTime = new Date();
        printBanner();
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
        LOGGER.info("Discovering filters...");
        discoverFilterDefinitions();
        start();
    }

    public static Server newInstance() {
        return newInstance(3000, 10);
    }

    public static Server newInstance(int port, int threadCount) {
        return new Server(port, threadCount);
    }

    private void start() {
        LOGGER.info("ready after " + (new Date().getTime() - startTime.getTime()) + "ms");
        LOGGER.info("Server started...");
        isRunning = true;
        acceptConnections();
    }

    private void discoverRouteDefinitions() {
        RouteDiscoverer.discoverRoutes("");
    }

    private void discoverFilterDefinitions() {
        FilterDiscoverer.discover();
    }

    private void acceptConnections() {
        while (isRunning) {
            try (Socket client = server.accept()) {
                threadPool.execute(() -> {
                    try {
                        handleConnection(client);
                    } catch (IOException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException e) {
                        LOGGER.trace(e);
                    }
                });
            } catch (IOException e) {
                LOGGER.trace(e);
            }
        }
    }

    private void handleConnection(Socket client) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        Request req = Request.parse(in);
        FilterManager.getInstance().runPreFilters(req);
        RouteManager.getInstance().getHandler(req.getType(), req.getPath()).handle(req, new Response(client));
    }

    public void stop() {
        isRunning = false;
    }

    public void addRoute(RequestTypes type, String route, IRequestHandler handler) {
        RouteManager.getInstance().add(type, route, handler);
    }

    public void get(String route, IRequestHandler handler) {
        addRoute(RequestTypes.GET, route, handler);
    }

    public void post(String route, IRequestHandler handler) {
        addRoute(RequestTypes.POST, route, handler);
    }

    public void addPreFilter(PreFilter filter) {
        FilterManager.getInstance().addPreFilter(filter);
    }

    public void addPostFilter(PostFilter filter) {
        FilterManager.getInstance().addPostFilter(filter);
    }

    private void printBanner() {
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
}
