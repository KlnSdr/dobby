package dobby.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RouteHelper {
    public static Tupel<String, List<String>> extractPathParams(String path) {
        List<String> params = new ArrayList<>();
        String[] parts = path.split("/");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("{") && parts[i].endsWith("}")) {
                params.add(parts[i].substring(1, parts[i].length() - 1));
                parts[i] = "*";
            }
        }

        String processedPath = String.join("/", parts).toLowerCase();
        if (processedPath.isEmpty()) {
            processedPath = "/";
        }

        return new Tupel<>(processedPath, params);
    }

    public static boolean matches(String actualPath, String route) {
        Pattern pattern = prepareRoutePattern(route);
        return pattern.matcher(actualPath).matches();
    }

    private static Pattern prepareRoutePattern(String path) {
        path = path.replace("*", "[^/]*");
        return Pattern.compile(path);
    }
}
