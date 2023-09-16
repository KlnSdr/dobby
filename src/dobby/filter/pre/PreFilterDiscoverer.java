package dobby.filter.pre;

import dobby.filter.FilterManager;
import dobby.util.Classloader;

public class PreFilterDiscoverer extends Classloader<PreFilter> {
    private PreFilterDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    public static void discover(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }

        PreFilterDiscoverer discoverer = new PreFilterDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::addFilter);

        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> PreFilterDiscoverer.discover(finalRootPackage + "." + subpackage));
    }

    @Override
    protected Class<? extends PreFilter> filterClasses(String line) {
        return defaultImplementsFilter(line, PreFilter.class);
    }

    private void addFilter(Class<? extends PreFilter> clazz) {
        try {
            PreFilter filter = clazz.getDeclaredConstructor().newInstance();
            FilterManager.getInstance().addPreFilter(filter);
            System.out.println("Added pre-filter: " + clazz.getName());
        } catch (Exception e) {
            System.err.println("Could not instantiate pre-filter: " + clazz.getName());
        }
    }
}
