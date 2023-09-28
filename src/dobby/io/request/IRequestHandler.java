package dobby.io.request;

import dobby.io.HttpContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface IRequestHandler {
    void handle(HttpContext context) throws IOException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException;
}
