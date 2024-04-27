package dobby.util;

import dobby.util.logging.Logger;

import java.io.Serializable;
import java.util.HashMap;

public class NewJson implements Serializable {
    private static final Logger LOGGER = new Logger(NewJson.class);
    private final HashMap<String, String> stringData = new HashMap<>();
    private final HashMap<String, NewJson> jsonData = new HashMap<>();

    public static NewJson parse(String raw) {
        final NewJson json = new NewJson();
        System.out.println(raw);
        int depth = -1;

        String key = null;


        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == '{') {
                depth++;
                if (depth == 0) {
                    continue;
                }
                final Tupel<String, Integer> res = extractNextJson(raw, i);

                if (res == null) {
                    break;
                }
                i = res._2();

                json.jsonData.put(key, parse(res._1()));
                key = null;
                depth--;
            } else if (c == '}') {
                depth--;
            } else {
                if (c == '"') {
                    final Tupel<String, Integer> res = extractNextString(raw, i);
                    if (res == null) {
                        break;
                    }

                    if (key == null) {
                        key = res._1();
                        System.out.println("Key: " + key);
                        i = findKeyValueDelimiter(raw, res._2());
                    } else {
                        final String value = res._1();
                        System.out.println("Value: " + value);
                        json.stringData.put(key, value);
                        key = null;
                        i = res._2();
                    }
                }
            }
            System.out.println(c);
        }

        if (depth != -1) {
            LOGGER.error("Invalid JSON");
            LOGGER.error(raw);
            return null;
        }

        return json;
    }

    private static Tupel<String, Integer> extractNextString(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset + 1; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == '"') {
                return new Tupel<>(sb.toString(), i);
            }
            sb.append(c);
        }
        return null;
    }

    private static int findKeyValueDelimiter(String raw, int offset) {
        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ':') {
                return i;
            }
        }
        return raw.length();
    }

    private static Tupel<String, Integer> extractNextJson(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();
        int depth = 0;

        for (int i = offset + 1; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                if (depth == 0) {
                    return new Tupel<>(sb.toString(), i);
                }
                depth--;
            }
            sb.append(c);
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("{");

        for (String key : stringData.keySet()) {
            sb.append("\"").append(key).append("\": \"").append(stringData.get(key)).append("\", ");
        }

        for (String key : jsonData.keySet()) {
            sb.append("\"").append(key).append("\": ").append(jsonData.get(key)).append(", ");
        }

        if (sb.length() > 1) {
            sb.delete(sb.length() - 2, sb.length());
        }

        sb.append("}");

        return sb.toString();
    }
}
