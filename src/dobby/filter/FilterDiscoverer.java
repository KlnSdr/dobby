package dobby.filter;

import dobby.filter.post.PostFilterDiscoverer;
import dobby.filter.pre.PreFilterDiscoverer;


public class FilterDiscoverer {
    public static void discover() {
        PreFilterDiscoverer.discover("");
        PostFilterDiscoverer.discover("");
    }
}
