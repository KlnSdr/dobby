package dobby.DefaultHandler;

import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;
import dobby.io.response.ResponseCodes;

import java.io.IOException;

/**
 * Handler for requests with unsupported methods
 */
public class MethodNotSupportedHandler implements IRequestHandler {
    @Override
    public void handle(HttpContext context) {
        context.getResponse().setCode(ResponseCodes.METHOD_NOT_ALLOWED);
    }
}
