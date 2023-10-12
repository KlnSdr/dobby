package com.klnsdr.dobby.routes.filter;

import com.klnsdr.dobby.filter.Filter;
import com.klnsdr.dobby.filter.FilterType;
import com.klnsdr.dobby.io.HttpContext;

public class CleanRoutePreFilter implements Filter {
    @Override
    public String getName() {
        return "clean-routes";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean run(HttpContext ctx) {
        String path = ctx.getRequest().getPath();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        ctx.getRequest().setPath(path);
        return true;
    }
}
