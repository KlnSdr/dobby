package dobby.routes;

import dobby.Request;
import dobby.RequestTypes;
import dobby.Response;
import dobby.Server;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.util.Classloader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class RouteDiscoverer extends Classloader<Object> {
    private RouteDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    public static void discoverRoutes(Server server, String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        RouteDiscoverer discoverer = new RouteDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(clazz -> discoverer.analyzeClassAndMethods(clazz, server));
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> RouteDiscoverer.discoverRoutes(server, finalRootPackage + "." + subpackage));
    }

    private void analyzeClassAndMethods(Class<?> clazz, Server server) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!isValidHttpHandler(method)) {
                continue;
            }

            if (method.isAnnotationPresent(Get.class)) {
                Get annotation = method.getAnnotation(Get.class);
                RouteManager.getInstance().add(RequestTypes.GET, annotation.route(), (req, res) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), req, res));
            } else if (method.isAnnotationPresent(Post.class)) {
                Post annotation = method.getAnnotation(Post.class);
                RouteManager.getInstance().add(RequestTypes.POST, annotation.route(), (req, res) -> method.invoke(clazz.getDeclaredConstructor().newInstance(), req, res));
            }
        }
    }

    private boolean isValidHttpHandler(Method method) {
        Type[] types = method.getParameterTypes();
        return types.length == 2 && types[0].equals(Request.class) && types[1].equals(Response.class);
    }

    @Override
    protected Class<?> filterClasses(String line) {
        return defaultClassFilter(line);
    }
}
