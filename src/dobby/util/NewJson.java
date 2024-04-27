package dobby.util;

import dobby.exceptions.MalformedJsonException;
import dobby.util.logging.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class NewJson implements Serializable {
    private static boolean SILENT_EXCEPTIONS = false;
    private static final Logger LOGGER = new Logger(NewJson.class);
    private static final String[] VALUE_DELIMITERS = new String[] {"{", "}", "[", "]", ":", "\""};

    private final HashMap<String, String> stringData = new HashMap<>();
    private final HashMap<String, NewJson> jsonData = new HashMap<>();

    public static void setSilentExceptions(boolean silentExceptions) {
        SILENT_EXCEPTIONS = silentExceptions;
    }

    public static NewJson parse(String raw) throws MalformedJsonException {
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

                if (res == null) {
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
                        } else if(!hasValueKeyDelimiter(raw, i)) {
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

    private static boolean hasValueKeyDelimiter(String raw, int offset) {
        for (int i = offset - 1; i > 0; i--) {
            final char c = raw.charAt(i);

            if (c == ',') {
                return true;
            } else if (Arrays.stream(VALUE_DELIMITERS).anyMatch(d -> d.equals(String.valueOf(c)))) {
                return false;
            }
        }

        return false;
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

    public void setString(String key, String value) {
        stringData.put(key, value);
    }

    public void setJson(String key, NewJson value) {
        jsonData.put(key, value);
    }

    public String getString(String key) {
        return stringData.get(key);
    }

    public NewJson getJson(String key) {
        return jsonData.get(key);
    }

    public boolean hasKey(String key) {
        return stringData.containsKey(key) || jsonData.containsKey(key);
    }

    public Set<String> getStringKeys() {
        return stringData.keySet();
    }

    public Set<String> getJsonKeys() {
        return jsonData.keySet();
    }

    public Set<String> getKeys() {
        final Set<String> keys = stringData.keySet();
        keys.addAll(jsonData.keySet());
        return keys;
    }
}
