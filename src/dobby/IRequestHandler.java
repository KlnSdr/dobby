package dobby;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface IRequestHandler {
    void handle(Request req, Response res) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
