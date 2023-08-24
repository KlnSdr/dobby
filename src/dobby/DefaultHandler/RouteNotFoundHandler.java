package dobby.DefaultHandler;

import dobby.IRequestHandler;
import dobby.Request;
import dobby.Response;
import dobby.ResponseCodes;

import java.io.IOException;

public class RouteNotFoundHandler implements IRequestHandler {
    public void handle(Request req, Response res) throws IOException {
        res.setCode(ResponseCodes.NOT_FOUND);
        res.setBody(String.format("Requested route %s not found", req.getPath()));
        res.send();
    }
}
