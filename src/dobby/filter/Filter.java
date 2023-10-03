package dobby.filter;

import dobby.io.HttpContext;

public interface Filter {
    String getName();

    FilterType getType();

    int getOrder();

    boolean run(HttpContext ctx);
}
