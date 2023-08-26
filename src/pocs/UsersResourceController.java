package pocs;

import dobby.Request;
import dobby.Response;
import dobby.ResponseCodes;
import dobby.annotations.Post;

import java.io.IOException;

public class UsersResourceController {
    @Post(route = "/users/create")
    public void createNewUser(Request req, Response res) throws IOException {
        System.out.println("Creating new user...");

        res.setCode(ResponseCodes.OK);
        res.send();
    }
}
