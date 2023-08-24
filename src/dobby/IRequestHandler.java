package dobby;

import java.io.IOException;

public interface IRequestHandler {
    void handle(Request req, Response res) throws IOException;
}
