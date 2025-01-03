package dobby.routes.filter;

import dobby.filter.Filter;
import dobby.filter.FilterOrder;
import dobby.filter.FilterType;
import dobby.io.HttpContext;

/**
 * Cleans route paths
 */
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
        return FilterOrder.CLEAN_ROUTES_PRE_FILTER.getOrder();
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
