package dobby;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Json {
    private final Map<String, String> data = new HashMap<>();

    /**
     * parses a given json string into a Json object
     *
     * @param raw the json string to parse
     * @return a new parsed Json object
     */
    public static Json parse(String raw) {
        raw = raw.trim();
        if (raw.isEmpty()) {
            return new Json();
        }

        // https://stackoverflow.com/a/45167612
        Pattern regex = Pattern.compile("[{\\[]{1}([,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]|\".*?\")+[}\\]]{1}");
        Matcher matcher = regex.matcher(raw);

        if (!matcher.find()) {
            return new Json();
        }

        raw = raw.substring(1);
        raw = raw.substring(0, raw.length() - 1);

        return parseJson(raw);
    }

    private static Json parseJson(String raw) {
        Json body = new Json();

        boolean isInString = false;
        StringBuilder buffer = new StringBuilder();

        String key = "";
        String value;

        for (int i = 0; i < raw.length(); i++) {
            if (isInString) {
                if (raw.charAt(i) != '"') {
                    buffer.append(raw.charAt(i));
                } else if (key.isEmpty()) {
                    isInString = false;
                    key = buffer.toString();
                    buffer = new StringBuilder();
                } else {
                    isInString = false;
                    value = buffer.toString();
                    body.set(key, value);
                    key = "";
                    buffer = new StringBuilder();
                }
            } else if (raw.charAt(i) == '"') {
                isInString = true;
            }
        }

        if (!key.isEmpty()) {
            body.set(key, buffer.toString());
        }

        return body;
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void set(String key, String value) {
        data.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public String get(String key) {
        return this.data.get(key);
    }

    /**
     * checks if the given key exists
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return this.data.containsKey(key);
    }

    /**
     * checks if all the given keys exist
     *
     * @param keys the keys to check
     * @return true if all the keys exist, false otherwise
     */
    public boolean hasKeys(String[] keys) {
        boolean result = true;
        for (String key : keys) {
            result = result & hasKey(key);
        }
        return result & keys.length > 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        for (String key : this.data.keySet()) {
            builder.append("\"").append(key).append("\":\"").append(this.data.get(key)).append("\",");
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }
}
