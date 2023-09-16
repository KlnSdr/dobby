package dobby.filter;

import dobby.Request;
import dobby.Response;
import dobby.logging.Logger;
import dobby.filter.post.PostFilter;
import dobby.filter.pre.PreFilter;

import java.util.Arrays;
import java.util.Comparator;

public class FilterManager {
    private static FilterManager instance;
    private final Logger LOGGER = new Logger(FilterManager.class);
    private PreFilter[] preFilters = new PreFilter[0];
    private PostFilter[] postFilters = new PostFilter[0];

    private FilterManager() {
    }

    public static FilterManager getInstance() {
        if (FilterManager.instance == null) {
            FilterManager.instance = new FilterManager();
        }
        return FilterManager.instance;
    }

    public void addPreFilter(PreFilter filter) {
        PreFilter[] newPreFilters = new PreFilter[this.preFilters.length + 1];
        System.arraycopy(this.preFilters, 0, newPreFilters, 0, this.preFilters.length);
        newPreFilters[this.preFilters.length] = filter;
        sortFilters(newPreFilters);
        this.preFilters = newPreFilters;

        LOGGER.debug(String.format("Added pre-filter %s", filter.getClass().getCanonicalName()));
    }

    public void addPostFilter(PostFilter filter) {
        PostFilter[] newPostFilters = new PostFilter[this.postFilters.length + 1];
        System.arraycopy(this.postFilters, 0, newPostFilters, 0, this.postFilters.length);
        newPostFilters[this.postFilters.length] = filter;
        sortFilters(newPostFilters);
        this.postFilters = newPostFilters;

        LOGGER.debug(String.format("Added post-filter %s", filter.getClass().getCanonicalName()));
    }

    public void runPreFilters(Request request) {
        Arrays.stream(preFilters).forEach(filter -> filter.run(request));
    }

    public void runPostFilters(Response response) {
        Arrays.stream(postFilters).forEach(filter -> filter.run(response));
    }

    private <V> void sortFilters(Filter<V>[] filters) {
        Arrays.sort(filters, Comparator.comparingInt(Filter::getOrder));
    }
}
