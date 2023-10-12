package com.klnsdr.dobby.routes;

import com.klnsdr.dobby.DefaultHandler.StaticFileHandler;
import com.klnsdr.dobby.io.request.IRequestHandler;
import com.klnsdr.dobby.io.request.RequestTypes;
import com.klnsdr.dobby.util.Tupel;
import com.klnsdr.dobby.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     *
     * @param type    The type of request to add the route for
     * @param path    The path to add the route for
     * @param handler The handler to add the route for
     */
    public void add(RequestTypes type, String path, IRequestHandler handler) {
        path = path.toLowerCase();
        Tupel<String, List<String>> pathAndParams = extractPathParams(path);
        path = pathAndParams._1();
        List<String> params = pathAndParams._2();

        if (!hasRoute(path)) {
            routes.put(path, new Route());
        }

        routes.get(path).addHandler(type, handler);
        routes.get(path).addPathParams(type, params);
        LOGGER.debug(String.format("Added route %s for %s", path, type.name()));
    }

    private Tupel<String, List<String>> extractPathParams(String path) {
        List<String> params = new ArrayList<>();
        String[] parts = path.split("/");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("{") && parts[i].endsWith("}")) {
                params.add(parts[i].substring(1, parts[i].length() - 1));
                parts[i] = "*";
            }
        }
        return new Tupel<>(String.join("/", parts), params);
    }

    /**
     * Gets the handler for the given request type and path
     *
     * @param type The type of request to get the handler for
     * @param path The path to get the handler for
     * @return The handler for the given request type and path
     */
    public Tupel<IRequestHandler, HashMap<String, String>> getHandler(RequestTypes type, String path) {
        path = path.toLowerCase();
        if (!hasRoute(path)) {
            return getHandlerMatchPaths(type, path);
        }

        return new Tupel<>(routes.get(path).getHandler(type), new HashMap<>());
    }

    private Tupel<IRequestHandler, HashMap<String, String>> getHandlerMatchPaths(RequestTypes type, String path) {
        List<String> patternPaths = routes.keySet().stream().filter(p -> p.contains("*")).collect(Collectors.toList());
        for (String p : patternPaths) {
            Pattern pattern = prepareRoutePattern(p);
            if (pattern.matcher(path).matches()) {
                Route route = routes.get(p);
                HashMap<String, String> pathParams = getPathParamValues(p, path, route.getPathParams(type));
                return new Tupel<>(route.getHandler(type), pathParams);
            }
        }
        return new Tupel<>(new StaticFileHandler(), new HashMap<>());
    }

    private HashMap<String, String> getPathParamValues(String pattern, String path, List<String> paramKeys) {
        HashMap<String, String> pathParams = new HashMap<>();
        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");
        int paramKeysIndex = 0;
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].equals("*")) {
                pathParams.put(paramKeys.get(paramKeysIndex), pathParts[i]);
                paramKeysIndex++;
            }
        }
        return pathParams;
    }

    private Pattern prepareRoutePattern(String path) {
        path = path.replace("*", "[^/]*");
        return Pattern.compile(path);
    }
}
