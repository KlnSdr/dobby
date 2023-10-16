package dobby.routes;

import dobby.DefaultHandler.MethodNotSupportedHandler;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;

import java.util.HashMap;
import java.util.List;

/**
 * The Route class is used to store information about a route
 */
public class Route {
    private final HashMap<RequestTypes, IRequestHandler> handlers = new HashMap<>();
    private final HashMap<RequestTypes, List<String>> pathParams = new HashMap<>();

    /**
     * Adds a handler to the route
     *
     * @param type    Request type
     * @param handler Request handler
     */
    public void addHandler(RequestTypes type, IRequestHandler handler) {
        handlers.put(type, handler);
    }

    /**
     * Adds path parameters to the route
     *
     * @param type   Request type
     * @param params Path parameters
     */
    public void addPathParams(RequestTypes type, List<String> params) {
        pathParams.put(type, params);
    }

    /**
     * Gets the path parameters of the route
     *
     * @param type Request type
     * @return Path parameters
     */
    public List<String> getPathParams(RequestTypes type) {
        return pathParams.get(type);
    }

    /**
     * Gets the handler of the route
     *
     * @param type Request type
     * @return Request handler
     */
    public IRequestHandler getHandler(RequestTypes type) {
        return handlers.getOrDefault(type, new MethodNotSupportedHandler());
    }
}
