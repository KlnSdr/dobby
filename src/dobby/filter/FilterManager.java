package dobby.filter;

import dobby.io.HttpContext;
import dobby.util.logging.Logger;

import java.util.Arrays;
import java.util.Comparator;

public class FilterManager {
    private static FilterManager instance;
    private final Logger LOGGER = new Logger(FilterManager.class);
    private Filter[] preFilters = new Filter[0];
    private Filter[] postFilters = new Filter[0];

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
        LOGGER.debug(String.format("Added pre-filter %s", filter.getClass().getCanonicalName()));
    }

    public void addPostFilter(Filter filter) {
        this.postFilters = addFilter(filter, this.postFilters);
        LOGGER.debug(String.format("Added post-filter %s", filter.getClass().getCanonicalName()));
    }

    private Filter[] addFilter(Filter filter, Filter[] filters) {
        Filter[] newFilters = new Filter[filters.length + 1];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        newFilters[filters.length] = filter;
        sortFilters(newFilters);
        filters = newFilters;
        return filters;
    }

    public void runPreFilters(HttpContext ctx) {
        Arrays.stream(preFilters).forEach(filter -> filter.run(ctx));
    }

    public void runPostFilters(HttpContext ctx) {
        Arrays.stream(postFilters).forEach(filter -> filter.run(ctx));
    }

    private void sortFilters(Filter[] filters) {
        Arrays.sort(filters, Comparator.comparingInt(Filter::getOrder));
    }
}
