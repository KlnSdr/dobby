package dobby.filter;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observable;
import dobby.observer.Observer;
import dobby.routes.IRouteManager;
import dobby.util.Tupel;
import common.logger.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Manages filters
 */
@RegisterFor(IFilterManager.class)
public class FilterManager implements Observable<Filter>, IFilterManager {
    private final Logger LOGGER = new Logger(FilterManager.class);
    private Filter[] preFilters = new Filter[0];
    private int preFilterCount = 0;
    private Filter[] postFilters = new Filter[0];
    private int postFilterCount = 0;
    private final IRouteManager routeManager;

    @Inject
    public FilterManager(IRouteManager routeManager) {
        this.routeManager = routeManager;
    }

    /**
     * Adds a pre filter to the filter chain
     *
     * @param filter The filter to add
     */
    public void addPreFilter(Filter filter) {
        this.preFilters = addFilter(filter, this.preFilters);
        preFilterCount = preFilters.length;
        LOGGER.debug(String.format("Added pre-filter %s", filter.getClass().getCanonicalName()));
    }

    /**
     * Adds a post filter to the filter chain
     *
     * @param filter The filter to add
     */
    public void addPostFilter(Filter filter) {
        this.postFilters = addFilter(filter, this.postFilters);
        postFilterCount = postFilters.length;
        LOGGER.debug(String.format("Added post-filter %s", filter.getClass().getCanonicalName()));
    }

    /**
     * Runs the filter chain
     *
     * @param ctx The HttpContext to run the filter chain on
     * @throws IOException               If an IO error occurs
     * @throws InvocationTargetException If an error occurs while invoking a method
     * @throws NoSuchMethodException     If a method does not exist
     * @throws InstantiationException    If a class could not be instantiated
     * @throws IllegalAccessException    If a method could not be accessed
     */
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
                routeManager.getHandler(ctx.getRequest().getType(), ctx.getRequest().getPath());

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
        fireEvent(createEvent(filter));
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

    private final ArrayList<Observer<Filter>> observers = new ArrayList<>();
    @Override
    public void addObserver(Observer<Filter> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Filter> observer) {
        observers.remove(observer);
    }

    @Override
    public void fireEvent(Event<Filter> event) {
        observers.forEach(observer -> observer.onEvent(event));
    }

    private Event<Filter> createEvent(Filter filter) {
        return new Event<>(EventType.CREATED, filter);
    }
}
