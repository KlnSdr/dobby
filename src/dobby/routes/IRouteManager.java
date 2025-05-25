package dobby.routes;

import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;
import dobby.util.Tupel;

import java.util.HashMap;
import java.util.Map;

public interface IRouteManager {
    Map<String, Route> getAllRoutes();
    void add(RequestTypes type, String path, IRequestHandler handler);
    Tupel<IRequestHandler, HashMap<String, String>> getHandler(RequestTypes type, String path);
}
