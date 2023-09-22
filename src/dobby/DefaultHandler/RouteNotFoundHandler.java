package dobby.DefaultHandler;

import dobby.io.request.IRequestHandler;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;

import java.io.IOException;

public class RouteNotFoundHandler implements IRequestHandler {
    public void handle(Request req, Response res) throws IOException {
        res.setCode(ResponseCodes.NOT_FOUND);
        res.setBody(String.format("Requested route %s not found", req.getPath()));
        res.send();
    }
}
