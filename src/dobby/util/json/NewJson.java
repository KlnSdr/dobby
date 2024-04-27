package dobby.util.json;

import dobby.exceptions.MalformedJsonException;
import dobby.util.Tupel;
import dobby.util.logging.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static dobby.util.json.helper.DataExtractionHelper.*;

public class NewJson implements Serializable {
    private static final Logger LOGGER = new Logger(NewJson.class);
    private static boolean SILENT_EXCEPTIONS = false;
    // data maps
    private final HashMap<String, String> stringData = new HashMap<>();
    private final HashMap<String, NewJson> jsonData = new HashMap<>();
    private final HashMap<String, Integer> intData = new HashMap<>();
    private final HashMap<String, Double> floatData = new HashMap<>();
    private final HashMap<String, Boolean> boolData = new HashMap<>();

    /**
     * Set whether exceptions should be thrown or not.<br>
     * ATTENTION: If set to true, exceptions will not be thrown and the method will return null if the JSON is malformed.
     * This might lead to unexpected behavior/missing data.
     *
     * @param silentExceptions Whether exceptions should be thrown or not
     */
    public static void setSilentExceptions(boolean silentExceptions) {
        SILENT_EXCEPTIONS = silentExceptions;
    }

    /**
     * Parse a JSON string into a NewJson object.
     *
     * @param raw The raw JSON string
     * @return The parsed NewJson object or null
     * @throws MalformedJsonException If the JSON is malformed
     * @throws NumberFormatException  If a number could not be parsed
     */
    public static NewJson parse(String raw) throws MalformedJsonException, NumberFormatException {
        boolean isFirstKey = true;

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

                if (res == null || key == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!SILENT_EXCEPTIONS) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
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
                        LOGGER.error("Malformed JSON: " + raw);
                        if (!SILENT_EXCEPTIONS) {
                            throw new MalformedJsonException(raw);
                        }
                        return null;
                    }

                    if (key == null) {
                        if (isFirstKey) {
                            isFirstKey = false;
                        } else if (!hasValueKeyDelimiter(raw, i)) {
                            LOGGER.error("Malformed JSON: " + raw);
                            if (!SILENT_EXCEPTIONS) {
                                throw new MalformedJsonException(raw);
                            }
                            return null;
                        }

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
                } else if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
                    final Tupel<String, Integer> res = extractNextNumber(raw, i);
                    if (res == null || key == null) {
                        LOGGER.error("Malformed JSON: " + raw);
                        if (!SILENT_EXCEPTIONS) {
                            throw new MalformedJsonException(raw);
                        }
                        return null;
                    }

                    if (res._1().contains(".")) {
                        json.floatData.put(key, Double.parseDouble(res._1()));
                    } else {
                        json.intData.put(key, Integer.parseInt(res._1()));
                    }

                    key = null;
                    i = res._2();
                } else if (c == 't' || c == 'f') {
                    final Tupel<Boolean, Integer> res = extractNextBoolean(raw, i);

                    if (res == null || key == null) {
                        LOGGER.error("Malformed JSON: " + raw);
                        if (!SILENT_EXCEPTIONS) {
                            throw new MalformedJsonException(raw);
                        }
                        return null;
                    }

                    json.boolData.put(key, res._1());
                    key = null;
                    i = res._2();
                }
            }
            System.out.println(c);
        }

        if (depth != -1) {
            LOGGER.error("Malformed JSON: " + raw);
            if (!SILENT_EXCEPTIONS) {
                throw new MalformedJsonException(raw);
            }
            return null;
        }

        return json;
    }

    private static void appendData(StringBuilder sb, HashMap<String, ?> data) {
        for (String key : data.keySet()) {
            sb.append("\"").append(key).append("\": ").append(data.get(key)).append(", ");
        }
    }

    private static void cutLoop(NewJson src) {
        final Set<NewJson> visited = new HashSet<>();
        visited.add(src);
        cutLoop(src, visited);
    }

    private static void cutLoop(NewJson src, Set<NewJson> visited) {
        src.jsonData.forEach((key, json) -> {
            if (visited.contains(json)) {
                src.jsonData.put(key, new NewJson());
            } else {
                visited.add(json);
                cutLoop(json, visited);
            }
        });
    }

    @Override
    public String toString() {
        cutLoop(this);
        final StringBuilder sb = new StringBuilder();

        sb.append("{");

        appendData(sb, stringData);
        appendData(sb, jsonData);
        appendData(sb, intData);
        appendData(sb, floatData);
        appendData(sb, boolData);

        if (sb.length() > 1) {
            sb.delete(sb.length() - 2, sb.length());
        }

        sb.append("}");

        return sb.toString();
    }

    // setter =======================================================================================

    public void setString(String key, String value) {
        stringData.put(key, value);
    }

    public void setJson(String key, NewJson value) {
        jsonData.put(key, value);
    }

    public void setInt(String key, int value) {
        intData.put(key, value);
    }

    public void setFloat(String key, double value) {
        floatData.put(key, value);
    }

    public void setBoolean(String key, boolean value) {
        boolData.put(key, value);
    }

    // getter =======================================================================================

    public String getString(String key) {
        return getValue(key, stringData);
    }

    public NewJson getJson(String key) {
        return getValue(key, jsonData);
    }

    public int getInt(String key) {
        return intData.get(key);
    }

    public double getFloat(String key) {
        return floatData.get(key);
    }

    public boolean getBoolean(String key) {
        return boolData.get(key);
    }

    private <T> T getValue(String key, HashMap<String, T> data) {
        final NewJson target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return null;
        }
        return data.get(key.split("\\.")[key.split("\\.").length - 1]);
    }

    // has key =======================================================================================

    public boolean hasKeys(String... keys) {
        for (String key : keys) {
            if (!hasKey(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasKey(String key) {
        final NewJson target = getTargetJsonObjectFromPath(key);
        if (target == null) {
            return false;
        }
        final String pathKey = key.split("\\.")[key.split("\\.").length - 1];
        return stringData.containsKey(pathKey) || jsonData.containsKey(pathKey) || intData.containsKey(pathKey) || floatData.containsKey(pathKey) || boolData.containsKey(pathKey);
    }

    // get keys =======================================================================================

    public Set<String> getStringKeys() {
        return stringData.keySet();
    }

    public Set<String> getJsonKeys() {
        return jsonData.keySet();
    }

    public Set<String> getIntKeys() {
        return intData.keySet();
    }

    public Set<String> getFloatKeys() {
        return floatData.keySet();
    }

    public Set<String> getBooleanKeys() {
        return boolData.keySet();
    }

    public Set<String> getKeys() {
        final Set<String> keys = stringData.keySet();
        keys.addAll(jsonData.keySet());
        keys.addAll(intData.keySet());
        keys.addAll(floatData.keySet());
        keys.addAll(boolData.keySet());
        return keys;
    }


    private NewJson getTargetJsonObjectFromPath(String key) {
        String[] path = key.split("\\.");
        NewJson target = this;
        for (int i = 0; i < path.length - 1; i++) {
            target = target.getJson(path[i]);
            if (target == null) {
                return null;
            }
        }
        return target;
    }
}
