package dobby.DefaultHandler;

import dobby.IRequestHandler;
import dobby.Request;
import dobby.Response;
import dobby.ResponseCodes;

import java.io.IOException;

public class MethodNotSupportedHandler implements IRequestHandler {
    @Override
    public void handle(Request req, Response res) throws IOException {
        res.setCode(ResponseCodes.METHOD_NOT_ALLOWED);
        res.send();
    }
}
