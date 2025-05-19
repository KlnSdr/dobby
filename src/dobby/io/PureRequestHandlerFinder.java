package dobby.io;

import common.util.Classloader;
import dobby.Dobby;
import common.logger.Logger;

/**
 * Discovers PureRequestHandler classes
 */
public class PureRequestHandlerFinder extends Classloader<PureRequestHandler> {
    private static final Logger LOGGER = new Logger(PureRequestHandlerFinder.class);

    private PureRequestHandlerFinder(String packageName) {
        this.packageName = packageName;
    }

    public static void discover(String rootPackage, Dobby dobby) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        PureRequestHandlerFinder discoverer = new PureRequestHandlerFinder(rootPackage);
        discoverer.loadClasses().forEach((clazz) -> discoverer.analyzeClass(clazz, dobby));
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> PureRequestHandlerFinder.discover(finalRootPackage + "." + subpackage, dobby));
    }

    private void analyzeClass(Class<? extends PureRequestHandler> clazz, Dobby dobby) {
        try {
            PureRequestHandler handler = clazz.getDeclaredConstructor().newInstance();
            dobby.registerPureRequestHandler(handler);
        } catch (Exception e) {
            LOGGER.error("Could not instantiate pure request handler: " + clazz.getName());
            LOGGER.trace(e);
        }
    }

    @Override
    protected Class<? extends PureRequestHandler> filterClasses(String line) {
        return defaultImplementsFilter(line, PureRequestHandler.class);
    }
}
