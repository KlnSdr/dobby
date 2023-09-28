package dobby.filter;

import dobby.io.HttpContext;

public interface Filter {
    String getName();

    FilterType getType();

    int getOrder();

    void run(HttpContext ctx);
}
