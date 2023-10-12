package com.klnsdr.dobby.filter;

import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.io.request.IRequestHandler;
import com.klnsdr.dobby.routes.RouteManager;
import com.klnsdr.dobby.util.Tupel;
import com.klnsdr.dobby.util.logging.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages filters
 */
public class FilterManager {
    private static FilterManager instance;
    private final Logger LOGGER = new Logger(FilterManager.class);
    private Filter[] preFilters = new Filter[0];
    private int preFilterCount = 0;
    private Filter[] postFilters = new Filter[0];
    private int postFilterCount = 0;

    private FilterManager() {
    }

    public static FilterManager getInstance() {
        if (FilterManager.instance == null) {
            FilterManager.instance = new FilterManager();
        }
        return FilterManager.instance;
    }

    public void addPreFilter(Filter filter) {
        this.preFilters = addFilter(filter, this.preFilters);
        preFilterCount = preFilters.length;
        LOGGER.debug(String.format("Added pre-filter %s", filter.getClass().getCanonicalName()));
    }

    public void addPostFilter(Filter filter) {
        this.postFilters = addFilter(filter, this.postFilters);
        postFilterCount = postFilters.length;
        LOGGER.debug(String.format("Added post-filter %s", filter.getClass().getCanonicalName()));
    }

    public void runFilterChain(HttpContext ctx) throws IOException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        int i = 0;
        while (i < preFilterCount) {
            boolean result = preFilters[i].run(ctx);
            if (!result) {
                break;
            }
            i++;
        }
        if (ctx.getResponse().didSend()) {
            return;
        } else if (i < preFilterCount) {
            ctx.getResponse().send();
            return;
        }

        Tupel<IRequestHandler, HashMap<String, String>> handler =
                RouteManager.getInstance().getHandler(ctx.getRequest().getType(), ctx.getRequest().getPath());

        ctx.getRequest().setPathParams(handler._2());
        handler._1().handle(ctx);

        i = 0;
        while (i < postFilterCount) {
            boolean result = postFilters[i].run(ctx);
            if (!result) {
                break;
            }
            i++;
        }

        if (!ctx.getResponse().didSend()) {
            ctx.getResponse().send();
        }
    }

    /**
     * Adds a filter to the given array of filters
     *
     * @param filter  The filter to add
     * @param filters The array of filters to add the filter to
     * @return The new array of filters
     */
    private Filter[] addFilter(Filter filter, Filter[] filters) {
        Filter[] newFilters = new Filter[filters.length + 1];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        newFilters[filters.length] = filter;
        sortFilters(newFilters);
        filters = newFilters;
        return filters;
    }

    /**
     * Sorts the given array of filters by their order
     *
     * @param filters The array of filters to sort
     */
    private void sortFilters(Filter[] filters) {
        Arrays.sort(filters, Comparator.comparingInt(Filter::getOrder));
    }
}
