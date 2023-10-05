package dobby.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Json {
    private final Map<String, String> stringData = new HashMap<>();
    private final Map<String, Integer> intData = new HashMap<>();
    private final Map<String, Json> jsonData = new HashMap<>();

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
                    body.setString(key, value);
                    key = "";
                    buffer = new StringBuilder();
                }
            } else if (raw.charAt(i) == '"') {
                isInString = true;
            }
        }

        if (!key.isEmpty()) {
            body.setString(key, buffer.toString());
        }

        return body;
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void setString(String key, String value) {
        stringData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public String getString(String key) {
        return this.stringData.get(key);
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void setInt(String key, Integer value) {
        intData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public Integer getInt(String key) {
        return this.intData.get(key);
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void setJson(String key, Json value) {
        jsonData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public Json getJson(String key) {
        return this.jsonData.get(key);
    }

    /**
     * checks if the given key exists
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return this.stringData.containsKey(key);
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
        String stringKeys = this.stringData.entrySet().stream().map(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            return "\"" + key + "\":\"" + value + "\"";
        }).collect(Collectors.joining(","));

        String intKeys = this.intData.entrySet().stream().map(entry -> {
            String key = entry.getKey();
            Integer value = entry.getValue();
            return "\"" + key + "\":" + value;
        }).collect(Collectors.joining(","));

        String jsonKeys = this.jsonData.entrySet().stream().map(entry -> {
            String key = entry.getKey();
            Json value = entry.getValue();
            return "\"" + key + "\":" + value.toString();
        }).collect(Collectors.joining(","));

        builder.append(stringKeys);
        if (!stringKeys.isEmpty() && (!intKeys.isEmpty() || !jsonKeys.isEmpty())) {
            builder.append(",");
        }

        builder.append(intKeys);
        if (!intKeys.isEmpty() && !jsonKeys.isEmpty()) {
            builder.append(",");
        }

        builder.append(jsonKeys);

        builder.append("}");
        return builder.toString();
    }
}
