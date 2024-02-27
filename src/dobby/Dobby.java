package dobby;

import dobby.files.service.StaticFileService;
import dobby.filter.FilterDiscoverer;
import dobby.filter.FilterManager;
import dobby.io.HttpContext;
import dobby.io.PureRequestHandler;
import dobby.io.PureRequestHandlerFinder;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.routes.RouteDiscoverer;
import dobby.session.Session;
import dobby.session.service.SessionService;
import dobby.task.SchedulerService;
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
    private static final String version = "0.1.2";
    private static Class<?> applicationClass;
    private final Logger LOGGER = new Logger(Dobby.class);
    private final Date startTime;
    private final String serverMode;
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    private PureRequestHandler pureRequestHandler;

    private Dobby(int port, int threadCount) {
        startTime = new Date();
        serverMode = Config.getInstance().getString("dobby.mode", "http").toLowerCase();
        if (!serverMode.equals("http") && !serverMode.equals("pure")) {
            LOGGER.error("invalid server mode: " + serverMode);
            System.exit(1);
            return;
        }

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            LOGGER.trace(e);
            System.exit(1);
            return;
        }
        threadPool = Executors.newFixedThreadPool(threadCount);
        LOGGER.info("Server initialized on port " + port + " with " + threadCount + " threads.");

        if (serverMode.equals("http")) {
            LOGGER.info("Discovering routes...");
            discoverRouteDefinitions();
            LOGGER.info("done!");

            if (!Config.getInstance().getBoolean("dobby.disableFilters")) {
                LOGGER.info("Discovering filters...");
                discoverFilterDefinitions();
            }
            LOGGER.info("done!");
        } else { // serverMode == pure, but since invalid options are rejected before "pure" is the only other option
            LOGGER.info("Pure mode enabled, no routes or filters will be discovered");
            LOGGER.info("registering pure request handler...");
            PureRequestHandlerFinder.discover("", this);
            if (pureRequestHandler == null) {
                LOGGER.error("no pure request handler found");
                System.exit(1);
                return;
            }
            LOGGER.info("done!");
        }
        start();
    }

    /**
     * Starts the server
     *
     * @param applicationClass The main entry point of the application
     */
    public static void startApplication(Class<?> applicationClass) {
        Dobby.applicationClass = applicationClass;
        printBanner();
        Config config = Config.getInstance();

        System.out.println("[" + config.getString("application.name", "<APP_NAME>") + "@" + config.getString(
                "application.version", "<APP_VERSION>") + "]");
        System.out.println();

        setLogLevel(config.getString("dobby.logLevel", "DEBUG"));

        runPreStart();

        StaticFileService.getInstance(); // initialize StaticFileService to start cleanup scheduler right at start
        SessionService.getInstance(); // initialize SessionService to start cleanup scheduler right at start

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

    /**
     * Gets the main class of the application
     *
     * @return The main class of the application
     */
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

    private static void runPreStart() {
        if (!DobbyEntryPoint.class.isAssignableFrom(getMainClass())) {
            return;
        }

        try {
            ((DobbyEntryPoint) getMainClass().getDeclaredConstructor().newInstance()).preStart();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            new Logger(Dobby.class).trace(e);
        }
    }

    private static void runPostStart() {
        if (!DobbyEntryPoint.class.isAssignableFrom(getMainClass())) {
            return;
        }

        try {
            ((DobbyEntryPoint) getMainClass().getDeclaredConstructor().newInstance()).postStart();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            new Logger(Dobby.class).trace(e);
        }
    }

    /**
     * Starts the server
     */
    private void start() {
        LOGGER.info("ready after " + (new Date().getTime() - startTime.getTime()) + "ms");
        LOGGER.info("Server started...");
        isRunning = true;
        runPostStart();
        registerStopHandler();
        acceptConnections();
    }

    private void discoverRouteDefinitions() {
        RouteDiscoverer.discoverRoutes("");
    }

    private void discoverFilterDefinitions() {
        FilterDiscoverer.discover("");
    }

    /**
     * Accepts connections and creates threads to handles them
     */
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

    /**
     * Handles a connection
     *
     * @param client The client to handle
     * @throws IOException               if an I/O error occurs when creating the input stream, the socket is closed,
     *                                   the socket is
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws NoSuchMethodException     if a matching method is not found.
     * @throws InstantiationException    if the class that declares the underlying method represents an abstract class.
     * @throws IllegalAccessException    if this Method object is enforcing Java language access control and the
     *                                   underlying method is inaccessible.
     */
    private void handleConnection(Socket client) throws IOException, InvocationTargetException, NoSuchMethodException
            , InstantiationException, IllegalAccessException {
        if (serverMode.equals("pure")) {
            if (pureRequestHandler == null) {
                LOGGER.error("no pure request handler found");
                return;
            }
            pureRequestHandler.onRequest(client);
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        HttpContext ctx = new HttpContext();

        Request req = Request.parse(in);
        Response res = new Response(client);

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
        SchedulerService.getInstance().stopAll();
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

    public void registerPureRequestHandler(PureRequestHandler pureRequestHandler) {
        if (this.pureRequestHandler != null) {
            LOGGER.error("multiple pure request handlers found!");
            LOGGER.error("overriding " + this.pureRequestHandler.getClass().getName() + " with " + pureRequestHandler.getClass().getName() + " would cause unexpected behavior");
            LOGGER.error("aborting...");
            System.exit(1);
            return;
        }
        this.pureRequestHandler = pureRequestHandler;
    }
}
