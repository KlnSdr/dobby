package pocs;

import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.Session;
import dobby.annotations.Get;
import dobby.session.SessionService;
import dobby.util.logging.Logger;

import java.io.IOException;

public class UsersResourceController {
    private final Logger LOGGER = new Logger(UsersResourceController.class);

    @Get("/users/create")
    public void createNewUser(Request req, Response res) throws IOException {
        LOGGER.debug("Creating new user...");

        Session session = req.getSession();

        if (session.get("user") != null) {
            res.setCode(ResponseCodes.FORBIDDEN);
            res.send();
            return;
        }

        session = SessionService.getInstance().newSession();

        session.set("user", "test");
        req.setSession(session);

        res.setCode(ResponseCodes.OK);
        res.send();
    }

    @Get("/users/info")
    public void getUserInfo(Request req, Response res) throws IOException {
        LOGGER.debug("Getting user info...");

        Session session = req.getSession();

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
    public void logout(Request req, Response res) throws IOException {
        LOGGER.debug("Logging out...");

        req.destroySession();

        res.setCode(ResponseCodes.OK);
        res.send();
    }
}
