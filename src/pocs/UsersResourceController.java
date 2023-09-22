package pocs;

import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import dobby.session.service.SessionService;
import dobby.util.logging.Logger;

import java.io.IOException;

public class UsersResourceController {
    private final Logger LOGGER = new Logger(UsersResourceController.class);

    @Get("/users/create")
    public void createNewUser(HttpContext context) throws IOException {
        Request req = context.getRequest();
        Response res = context.getResponse();

        LOGGER.debug("Creating new user...");

        Session session = context.getSession();

        if (session.get("user") != null) {
            res.setCode(ResponseCodes.FORBIDDEN);
            res.send();
            return;
        }

        session = SessionService.getInstance().newSession();

        session.set("user", "test");
        context.setSession(session);

        res.setCode(ResponseCodes.OK);
        res.send();
    }

    @Get("/users/info")
    public void getUserInfo(HttpContext context) throws IOException {
        Request req = context.getRequest();
        Response res = context.getResponse();

        LOGGER.debug("Getting user info...");

        Session session = context.getSession();

        if (session.get("user") == null) {
            res.setCode(ResponseCodes.UNAUTHORIZED);
            res.send();
            return;
        }

        res.setBody(session.get("user"));

        res.setCode(ResponseCodes.OK);
        res.send();
    }

    @Get("/users/logout")
    public void logout(HttpContext context) throws IOException {
        LOGGER.debug("Logging out...");
        context.destroySession();
        Response res = context.getResponse();

        res.setCode(ResponseCodes.OK);
        res.send();
    }
}
