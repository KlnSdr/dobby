package dobby.DefaultHandler;

import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;

import java.io.IOException;

/**
 * Handler for requests to routes that don't exist
 */
public class RouteNotFoundHandler implements IRequestHandler {
    public void handle(HttpContext context) throws IOException {
        Response res = context.getResponse();
        Request req = context.getRequest();

        res.setCode(ResponseCodes.NOT_FOUND);
        res.setBody(String.format("Requested route %s not found", req.getPath()));
        res.send();
    }
}
