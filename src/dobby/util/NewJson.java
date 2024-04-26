package dobby.util;

import dobby.util.logging.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewJson implements Serializable {
    private static final Logger LOGGER = new Logger(NewJson.class);
    private final HashMap<String, String> stringData = new HashMap<>();

    public static NewJson parse(String raw) {
        final NewJson json = new NewJson();
        System.out.println(raw);
        int depth = -1;
        final ArrayList<String> data = new ArrayList<>();

        for (char c : raw.toCharArray()) {
            if (c == '{') {
                depth++;
                data.add("");
            } else if (c == '}') {
                final Map<String, String> strings = extractStrings(data.get(depth).trim());
                json.stringData.putAll(strings);
                data.remove(depth);
                depth--;
            } else {
                data.set(depth, data.get(depth).concat(String.valueOf(c)));
            }
        }

        if (depth != -1) {
            LOGGER.error("Invalid JSON");
            LOGGER.error(raw);
            return null;
        }

        return json;
    }

    private static Map<String, String> extractStrings(String raw) {
        final HashMap<String, String> strings = new HashMap<>();
        final String[] parts = raw.split(",");

        for (String part : parts) {
            final String[] keyValue = part.split(":");
            for (int i = 0; i < keyValue.length; i++) {
                keyValue[i] = keyValue[i].trim();
            }

            if (keyValue.length != 2) {
                continue;
            }

            keyValue[0] = keyValue[0].replace("\"", "");

            if (keyValue[1].startsWith("\"") && keyValue[1].endsWith("\"")) {
                strings.put(keyValue[0].trim(), keyValue[1].substring(1, keyValue[1].length() - 1));
            }
        }
        return strings;
    }
}
