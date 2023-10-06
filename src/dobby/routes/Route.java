package dobby.routes;

import dobby.DefaultHandler.MethodNotSupportedHandler;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;

import java.util.HashMap;
import java.util.List;

public class Route {
    private final HashMap<RequestTypes, IRequestHandler> handlers = new HashMap<>();
    private final HashMap<RequestTypes, List<String>> pathParams = new HashMap<>();

    public void addHandler(RequestTypes type, IRequestHandler handler) {
        handlers.put(type, handler);
    }

    public void addPathParams(RequestTypes type, List<String> params) {
        pathParams.put(type, params);
    }

    public List<String> getPathParams(RequestTypes type) {
        return pathParams.get(type);
    }

    public IRequestHandler getHandler(RequestTypes type) {
        return handlers.getOrDefault(type, new MethodNotSupportedHandler());
    }
}
