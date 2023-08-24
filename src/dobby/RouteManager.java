package dobby;

import dobby.DefaultHandler.RouteNotFoundHandler;

import java.util.HashMap;

public class RouteManager {
    private static RouteManager instance;

    private final HashMap<String, Route> routes = new HashMap<>();

    private RouteManager() {}

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
    }

    public IRequestHandler getHandler(RequestTypes type, String path) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            return new RouteNotFoundHandler();
        }

        return routes.get(path).getHandler(type);
    }
}
