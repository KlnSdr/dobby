package dobby.routes;

import dobby.DefaultHandler.MethodNotSupportedHandler;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;

import java.util.HashMap;

public class Route {
    private final HashMap<RequestTypes, IRequestHandler> handlers = new HashMap<>();
    public void addHandler(RequestTypes type, IRequestHandler handler) {
        handlers.put(type, handler);
    }

    public IRequestHandler getHandler(RequestTypes type) {
        return handlers.getOrDefault(type, new MethodNotSupportedHandler());
    }
}
