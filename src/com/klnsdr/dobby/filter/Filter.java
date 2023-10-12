package com.klnsdr.dobby.filter;

import com.klnsdr.dobby.io.HttpContext;

public interface Filter {
    String getName();

    FilterType getType();

    int getOrder();

    boolean run(HttpContext ctx);
}
