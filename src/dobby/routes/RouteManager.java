package dobby.routes;

import common.inject.annotations.RegisterFor;
import dobby.DefaultHandler.MethodNotSupportedHandler;
import dobby.DefaultHandler.StaticFileHandler;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observable;
import dobby.observer.Observer;
import dobby.util.Tupel;
import common.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dobby.util.RouteHelper.extractPathParams;
import static dobby.util.RouteHelper.matches;

/**
 * Manages routes
 */
@RegisterFor(IRouteManager.class)
public class RouteManager implements Observable<Tupel<String, Route>>, IRouteManager {
    private final Logger LOGGER = new Logger(RouteManager.class);

    private final HashMap<String, Route> routes = new HashMap<>();

    public RouteManager() {
    }

    private boolean hasRoute(String path) {
        return routes.containsKey(path);
    }

    /**
     * Retrieves all routes managed by the RouteManager.
     *
     * @return A map containing all routes, where the key is the path and the value is the Route object.
     */
    public Map<String, Route> getAllRoutes() {
        return routes;
    }

    /**
     * Adds a route to the route manager
     *
     * @param type    The type of request to add the route for
     * @param path    The path to add the route for
     * @param handler The handler to add the route for
     */
    public void add(RequestTypes type, String path, IRequestHandler handler) {
        Tupel<String, List<String>> pathAndParams = extractPathParams(path);
        path = pathAndParams._1();
        List<String> params = pathAndParams._2();

        if (!hasRoute(path)) {
            routes.put(path, new Route());
        }

        routes.get(path).addHandler(type, handler);
        routes.get(path).addPathParams(type, params);
        LOGGER.debug(String.format("Added route %s for %s", path, type.name()));
        fireEvent(createEvent(path, routes.get(path)));
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

    /**
     * Gets the handler for the given request type and path based on pattern matching
     *
     * @param type The type of request to get the handler for
     * @param path The path to get the handler for
     * @return The handler for the given request type and path
     */
    private Tupel<IRequestHandler, HashMap<String, String>> getHandlerMatchPaths(RequestTypes type, String path) {
        List<String> patternPaths = routes.keySet().stream().filter(p -> p.contains("*")).collect(Collectors.toList());
        for (String p : patternPaths) {
            if (matches(path, p)) {
                Route route = routes.get(p);

                final List<String> paramKeys = route.getPathParams(type);
                if (paramKeys == null) { // route exists but no paramKeys exist for the given request type
                    return new Tupel<>(new MethodNotSupportedHandler(), new HashMap<>());
                }

                HashMap<String, String> pathParams = getPathParamValues(p, path, paramKeys);
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

    private final ArrayList<Observer<Tupel<String, Route>>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<Tupel<String, Route>> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Tupel<String, Route>> observer) {
        observers.remove(observer);
    }

    @Override
    public void fireEvent(Event<Tupel<String, Route>> event) {
        observers.forEach(observer -> observer.onEvent(event));
    }

    private Event<Tupel<String, Route>> createEvent(String path, Route route) {
        return new Event<>(EventType.CREATED, new Tupel<>(path, route));
    }
}
