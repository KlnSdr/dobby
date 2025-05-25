package dobby.filter;


import common.inject.InjectorService;
import common.util.Classloader;
import common.logger.Logger;


/**
 * Discovers filters in the classpath
 */
public class FilterDiscoverer extends Classloader<Filter> {
    private static final Logger LOGGER = new Logger(FilterDiscoverer.class);
    private final IFilterManager filterManager;

    private FilterDiscoverer(String packageName) {
        this.packageName = packageName;
        this.filterManager = InjectorService.getInstance().getInstance(IFilterManager.class);
    }

    /**
     * Discovers filters in the given package
     *
     * @param rootPackage The package to discover filters in
     */
    public static void discover(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }

        FilterDiscoverer discoverer = new FilterDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::addFilter);

        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> FilterDiscoverer.discover(finalRootPackage + "." + subpackage));
    }

    @Override
    protected Class<? extends Filter> filterClasses(String line) {
        return defaultImplementsFilter(line, Filter.class);
    }

    private void addFilter(Class<? extends Filter> clazz) {
        try {
            Filter filter = clazz.getDeclaredConstructor().newInstance();

            if (filter.getType() == FilterType.PRE) {
                filterManager.addPreFilter(filter);
                return;
            }
            filterManager.addPostFilter(filter);
        } catch (Exception e) {
            LOGGER.error("Could not instantiate post-filter: " + clazz.getName());
            LOGGER.trace(e);
        }
    }
}
