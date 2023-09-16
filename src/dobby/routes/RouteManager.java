package dobby.routes;

import dobby.DefaultHandler.RouteNotFoundHandler;
import dobby.IRequestHandler;
import dobby.RequestTypes;
import dobby.util.logging.Logger;

import java.util.HashMap;

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

    public void add(RequestTypes type, String path, IRequestHandler handler) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            routes.put(path, new Route());
        }

        routes.get(path).addHandler(type, handler);
        LOGGER.debug(String.format("Added route %s for %s", path, type.name()));
    }

    public IRequestHandler getHandler(RequestTypes type, String path) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            return new RouteNotFoundHandler();
        }

        return routes.get(path).getHandler(type);
    }
}
