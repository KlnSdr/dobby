package dobby.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Json class
 */
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
        Pattern regex = Pattern.compile("[{\\[]([,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]|\".*?\")+[}\\]]");
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
        boolean isInNumber = false;
        StringBuilder buffer = new StringBuilder();

        String key = "";
        String value;

        for (int i = 0; i < raw.length(); i++) {
            char currentChar = raw.charAt(i);

            if (isInString) {
                if (currentChar != '"') {
                    buffer.append(currentChar);
                } else {
                    isInString = false;
                    if (key.isEmpty()) {
                        key = buffer.toString();
                    } else {
                        value = buffer.toString();
                        body.setString(key, value);
                        key = "";
                    }
                    buffer = new StringBuilder();
                }
            } else if (isInNumber || Character.isDigit(currentChar) || (buffer.length() == 0 && currentChar == '-')) {
                isInNumber = true;
                if (Character.isDigit(currentChar) || currentChar == '-') {
                    buffer.append(currentChar);
                } else {
                    isInNumber = false;
                    value = buffer.toString();
                    body.setInt(key, Integer.parseInt(value));
                    key = "";
                    buffer = new StringBuilder();
                }
            } else if (currentChar == '"') {
                isInString = true;
            } else if (currentChar == '{') {
                buffer.append(currentChar);
                int openingBraces = 1;
                while (openingBraces > 0) {
                    i++;
                    char nestedChar = raw.charAt(i);
                    buffer.append(nestedChar);
                    if (nestedChar == '{') {
                        openingBraces++;
                    } else if (nestedChar == '}') {
                        openingBraces--;
                    }
                }
                value = buffer.toString();
                body.setJson(key, parseJson(value.substring(1, value.length() - 1)));
                key = "";
                buffer = new StringBuilder();
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
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            this.stringData.put(key, value);
            return;
        }
        target.stringData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public String getString(String key) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return null;
        }
        return target.stringData.get(key.split("\\.")[key.split("\\.").length - 1]);
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void setInt(String key, Integer value) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            this.intData.put(key, value);
            return;
        }
        target.intData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public Integer getInt(String key) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return null;
        }
        return target.intData.get(key.split("\\.")[key.split("\\.").length - 1]);
    }

    /**
     * sets the given key to the provided value
     *
     * @param key   the key to set
     * @param value the value to set
     */
    public void setJson(String key, Json value) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return;
        }
        target.jsonData.put(key, value);
    }

    /**
     * gets the value of the given key
     *
     * @param key the key to get
     * @return the value for the given key or null
     */
    public Json getJson(String key) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return null;
        }
        return target.jsonData.get(key.split("\\.")[key.split("\\.").length - 1]);
    }

    /**
     * checks if the given key exists
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        Json target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return false;
        }
        return target.stringData.containsKey(key);
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

    private Json getTargetJsonObjectFromPath(String key) {
        String[] path = key.split("\\.");
        Json target = this;
        for (int i = 0; i < path.length - 1; i++) {
            target = target.getJson(path[i]);
            if (target == null) {
                return null;
            }
        }
        return target;
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
