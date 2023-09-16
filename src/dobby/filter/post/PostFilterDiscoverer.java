package dobby.filter.post;

import dobby.filter.FilterManager;
import dobby.util.Classloader;

public class PostFilterDiscoverer extends Classloader<PostFilter> {

    private PostFilterDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    public static void discover(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }

        PostFilterDiscoverer discoverer = new PostFilterDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::addFilter);

        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> PostFilterDiscoverer.discover(finalRootPackage + "." + subpackage));
    }

    @Override
    protected Class<? extends PostFilter> filterClasses(String line) {
        return defaultImplementsFilter(line, PostFilter.class);
    }

    private void addFilter(Class<? extends PostFilter> clazz) {
        try {
            PostFilter filter = clazz.getDeclaredConstructor().newInstance();
            FilterManager.getInstance().addPostFilter(filter);
        } catch (Exception e) {
            System.err.println("Could not instantiate post-filter: " + clazz.getName());
        }
    }
}
