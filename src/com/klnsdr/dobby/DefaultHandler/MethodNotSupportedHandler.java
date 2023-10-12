package com.klnsdr.dobby.DefaultHandler;

import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.io.request.IRequestHandler;
import com.klnsdr.dobby.io.response.ResponseCodes;

/**
 * Handler for requests with unsupported methods
 */
public class MethodNotSupportedHandler implements IRequestHandler {
    @Override
    public void handle(HttpContext context) {
        context.getResponse().setCode(ResponseCodes.METHOD_NOT_ALLOWED);
    }
}
