package com.klnsdr.dobby.DefaultHandler;

import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.io.request.IRequestHandler;
import com.klnsdr.dobby.io.request.Request;
import com.klnsdr.dobby.io.response.Response;
import com.klnsdr.dobby.io.response.ResponseCodes;

/**
 * Handler for requests to routes that don't exist
 */
public class RouteNotFoundHandler implements IRequestHandler {
    public void handle(HttpContext context) {
        Response res = context.getResponse();
        Request req = context.getRequest();

        res.setCode(ResponseCodes.NOT_FOUND);
        res.setBody(String.format("Requested route %s not found", req.getPath()));
    }
}
