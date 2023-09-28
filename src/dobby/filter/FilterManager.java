package dobby.filter;

import dobby.io.HttpContext;
import dobby.util.logging.Logger;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Manages filters
 */
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

    /**
     * Adds a filter to the given array of filters
     * @param filter The filter to add
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
     * Runs all pre-filters
     * @param ctx The HttpContext to run the filters on
     */
    public void runPreFilters(HttpContext ctx) {
        Arrays.stream(preFilters).forEach(filter -> filter.run(ctx));
    }

    /**
     * Runs all post-filters
     * @param ctx The HttpContext to run the filters on
     */
    public void runPostFilters(HttpContext ctx) {
        Arrays.stream(postFilters).forEach(filter -> filter.run(ctx));
    }

    /**
     * Sorts the given array of filters by their order
     * @param filters The array of filters to sort
     */
    private void sortFilters(Filter[] filters) {
        Arrays.sort(filters, Comparator.comparingInt(Filter::getOrder));
    }
}
