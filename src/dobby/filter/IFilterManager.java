package dobby.filter;

import dobby.io.HttpContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface IFilterManager {
    void addPreFilter(Filter filter);
    void addPostFilter(Filter filter);
    void runFilterChain(HttpContext context) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;
}
