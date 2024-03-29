package dobby.routes;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.request.RequestTypes;
import dobby.util.Classloader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Discovers routes in a given package
 */
public class RouteDiscoverer extends Classloader<Object> {
    private RouteDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Discovers routes in a given package
     *
     * @param rootPackage Root package
     */
    public static void discoverRoutes(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        RouteDiscoverer discoverer = new RouteDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::analyzeClassAndMethods);
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> RouteDiscoverer.discoverRoutes(finalRootPackage + "." + subpackage));
    }

    private void analyzeClassAndMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!isValidHttpHandler(method)) {
                continue;
            }

            if (method.isAnnotationPresent(Get.class)) {
                Get annotation = method.getAnnotation(Get.class);
                RouteManager.getInstance().add(RequestTypes.GET, annotation.value(),
                        (ctx) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), ctx));
            } else if (method.isAnnotationPresent(Post.class)) {
                Post annotation = method.getAnnotation(Post.class);
                RouteManager.getInstance().add(RequestTypes.POST, annotation.value(),
                        (ctx) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), ctx));
            } else if (method.isAnnotationPresent(Put.class)) {
                Put annotation = method.getAnnotation(Put.class);
                RouteManager.getInstance().add(RequestTypes.PUT, annotation.value(),
                        (ctx) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), ctx));
            } else if (method.isAnnotationPresent(Delete.class)) {
                Delete annotation = method.getAnnotation(Delete.class);
                RouteManager.getInstance().add(RequestTypes.DELETE, annotation.value(),
                        (ctx) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), ctx));
            }
        }
    }

    private boolean isValidHttpHandler(Method method) {
        Type[] types = method.getParameterTypes();
        return types.length == 1 && types[0].equals(HttpContext.class);
    }

    @Override
    protected Class<?> filterClasses(String line) {
        return defaultClassFilter(line);
    }
}
