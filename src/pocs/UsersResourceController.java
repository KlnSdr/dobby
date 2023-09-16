package pocs;

import dobby.Request;
import dobby.Response;
import dobby.ResponseCodes;
import dobby.annotations.Post;
import dobby.util.logging.Logger;

import java.io.IOException;

public class UsersResourceController {
    private final Logger LOGGER = new Logger(UsersResourceController.class);

    @Post(route = "/users/create")
    public void createNewUser(Request req, Response res) throws IOException {
        LOGGER.debug("Creating new user...");

        res.setCode(ResponseCodes.OK);
        res.send();
    }
}
