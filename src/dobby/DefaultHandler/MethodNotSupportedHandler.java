package dobby.DefaultHandler;

import dobby.io.request.IRequestHandler;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;

import java.io.IOException;

public class MethodNotSupportedHandler implements IRequestHandler {
    @Override
    public void handle(Request req, Response res) throws IOException {
        res.setCode(ResponseCodes.METHOD_NOT_ALLOWED);
        res.send();
    }
}
