package dobby;

import common.inject.InjectorService;
import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.exceptions.MalformedJsonException;
import dobby.exceptions.RequestTooBigException;
import dobby.files.service.IStaticFileService;
import dobby.filter.FilterDiscoverer;
import dobby.filter.IFilterManager;
import dobby.io.HttpContext;
import dobby.io.PureRequestHandler;
import dobby.io.PureRequestHandlerFinder;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.routes.RouteDiscoverer;
import dobby.session.ISession;
import dobby.session.ISessionStore;
import dobby.session.service.ISessionService;
import dobby.task.ISchedulerService;
import common.logger.LogLevel;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.io.IOException;
import java.io.InputStream;
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
@RegisterFor(Dobby.class)
public class Dobby {
    private static final String version = "2.3-snapshot";
    public static final int DEFAULT_MAX_REQUEST_SIZE = 1024 * 1024 * 10;
    private static Class<?> applicationClass;
    private static final Logger LOGGER = new Logger(Dobby.class);
    private Date startTime;
    private String serverMode;
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    private PureRequestHandler pureRequestHandler;
    private final ISchedulerService schedulerService;
    private final IFilterManager filterManager;
    private static final InjectorService injectorService = InjectorService.getInstance();
    private Integer port = null;
    private Integer threadCount = null;
    private final IConfig config;

    public static String getVersion() {
        return version;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Inject
    public Dobby(ISchedulerService schedulerService, IFilterManager filterManager, IConfig config) {
        this.schedulerService = schedulerService;
        this.filterManager = filterManager;
        this.config = config;
    }

    public void initialize() {
        printBanner();
        System.out.println("[" + config.getString("application.name", "<APP_NAME>") + "@" + config.getString(
                "application.version", "<APP_VERSION>") + "]");
        System.out.println();

        setLogLevel(config.getString("dobby.logLevel", "DEBUG"));

        runPreStart();

        final ISessionService sessionService = injectorService.getInstance(ISessionService.class);

        configureSessionStore(sessionService);

        injectorService.getInstance(IStaticFileService.class).init(); // initialize StaticFileService to start cleanup scheduler right at start
        this.port = config.getInt("dobby.port", 3000);
        this.threadCount = config.getInt("dobby.threads", 10);
    }

    public void run() {
        serverMode = config.getString("dobby.mode", "http").toLowerCase();
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

            if (!config.getBoolean("dobby.disableFilters")) {
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
        final Dobby dobby = injectorService.getInstance(Dobby.class);

        final Date startTime = new Date();
        dobby.setStartTime(startTime);

        dobby.initialize();
        dobby.run();
    }

    private static void setLogLevel(String logLevelString) {
        LogLevel logLevel = LogLevel.DEBUG;
        try {
            logLevel = LogLevel.valueOf(logLevelString.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("invalid log level: " + logLevelString + ", using DEBUG");
        }
        Logger.setMaxLogLevel(logLevel);
    }

    private void configureSessionStore(ISessionService sessionService) {
        final String sessionStoreClassName = config.getString("dobby.session.store", "dobby.session.DefaultSessionStore");

        try {
            final Class<?> sessionStoreClass = Class.forName(sessionStoreClassName);
            if (!ISessionStore.class.isAssignableFrom(sessionStoreClass)) {
                throw new ClassCastException("session store class must implement ISessionStore");
            }

            final ISessionStore sessionStore = injectorService.getInstanceNullable(ISessionStore.class);
            if (sessionStore == null) {
                sessionService.setSessionStore((ISessionStore) sessionStoreClass.getDeclaredConstructor().newInstance());
            } else {
                sessionService.setSessionStore(sessionStore);
            }
            LOGGER.info("registered session store: " + sessionStoreClassName);
        } catch (Exception e) {
            LOGGER.error("invalid session store class: " + sessionStoreClassName);
            LOGGER.trace(e);
            System.exit(1);
        }
    }

    /**
     * Gets the main class of the application
     *
     * @return The main class of the application
     */
    public static Class<?> getMainClass() {
        return applicationClass;
    }

    private void printBanner() {
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

    private void runPreStart() {
        if (!DobbyEntryPoint.class.isAssignableFrom(getMainClass())) {
            return;
        }

        try {
            final DobbyEntryPoint entryPoint = injectorService.getInstanceNullable(DobbyEntryPoint.class);
            if (entryPoint != null) {
                entryPoint.preStart();
                return;
            }
            ((DobbyEntryPoint) getMainClass().getDeclaredConstructor().newInstance()).preStart();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            LOGGER.trace(e);
        }
    }

    private void runPostStart() {
        if (!DobbyEntryPoint.class.isAssignableFrom(getMainClass())) {
            return;
        }

        try {
            final DobbyEntryPoint entryPoint = injectorService.getInstanceNullable(DobbyEntryPoint.class);
            if (entryPoint != null) {
                entryPoint.postStart();
                return;
            }
            ((DobbyEntryPoint) getMainClass().getDeclaredConstructor().newInstance()).postStart();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            LOGGER.trace(e);
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
                             IllegalAccessException | MalformedJsonException e) {
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
            , InstantiationException, IllegalAccessException, MalformedJsonException {
        if (serverMode.equals("pure")) {
            if (pureRequestHandler == null) {
                LOGGER.error("no pure request handler found");
                return;
            }
            pureRequestHandler.onRequest(client);
            return;
        }

        final InputStream in = client.getInputStream();

        final HttpContext ctx = new HttpContext();

        final Response res = new Response(client);
        final Request req;

        try {
            req = Request.parse(in);
        } catch (RequestTooBigException e) {
            LOGGER.trace(e);

            res.setCode(ResponseCodes.CONTENT_TOO_LARGE);

            final NewJson json = new NewJson();
            json.setString("msg", "Request body too large. Max size: " + config.getInt("dobby.maxRequestSize", DEFAULT_MAX_REQUEST_SIZE) + " bytes");
            res.setBody(json);
            res.send();
            return;
        }

        ctx.setRequest(req);
        ctx.setResponse(res);
        ctx.setSession(injectorService.getNewInstanceNullable(ISession.class)); // if available, session will be set in SessionPreFilter

        try {
            filterManager.runFilterChain(ctx);
        } catch(Exception e) {
            LOGGER.trace(e);
            res.setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            res.setBody("Internal Server Error");
            res.send();
        }
    }

    /**
     * Stops the server
     */
    private void stop() {
        isRunning = false;
        LOGGER.info("Server stopping...");
        schedulerService.stopAll();
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
