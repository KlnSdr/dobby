package dobby.routes;

import dobby.DefaultHandler.StaticFileHandler;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;
import dobby.util.logging.Logger;

import java.util.HashMap;

/**
 * Manages routes
 */
public class RouteManager {
    private static RouteManager instance;
    private final Logger LOGGER = new Logger(RouteManager.class);

    private final HashMap<String, Route> routes = new HashMap<>();

    private RouteManager() {
    }

    public static RouteManager getInstance() {
        if (instance == null) {
            instance = new RouteManager();
        }
        return instance;
    }

    private boolean hasRoute(String path) {
        return routes.containsKey(path);
    }

    /**
     * Adds a route to the route manager
     * @param type The type of request to add the route for
     * @param path The path to add the route for
     * @param handler The handler to add the route for
     */
    public void add(RequestTypes type, String path, IRequestHandler handler) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            routes.put(path, new Route());
        }

        routes.get(path).addHandler(type, handler);
        LOGGER.debug(String.format("Added route %s for %s", path, type.name()));
    }

    /**
     * Gets the handler for the given request type and path
     * @param type The type of request to get the handler for
     * @param path The path to get the handler for
     * @return The handler for the given request type and path
     */
    public IRequestHandler getHandler(RequestTypes type, String path) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            return new StaticFileHandler();
        }

        return routes.get(path).getHandler(type);
    }
}
