package dobby.util.json.helper;

import dobby.util.Tupel;

import java.util.Arrays;

public class DataExtractionHelper {
    private static final String[] VALUE_DELIMITERS = new String[]{"{", "}", "[", "]", ":", "\""};

    public static Tupel<Boolean, Integer> extractNextBoolean(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ',' || Arrays.stream(VALUE_DELIMITERS).anyMatch(d -> d.equals(String.valueOf(c)))) {
                if (sb.toString().equals("true")) {
                    return new Tupel<>(true, i);
                } else if (sb.toString().equals("false")) {
                    return new Tupel<>(false, i);
                } else {
                    return null;
                }
            }
            sb.append(c);
        }
        return null;
    }

    public static Tupel<String, Integer> extractNextNumber(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ',' || Arrays.stream(VALUE_DELIMITERS).anyMatch(d -> d.equals(String.valueOf(c)))) {
                return new Tupel<>(sb.toString(), i);
            }
            sb.append(c);
        }
        return null;
    }

    public static Tupel<String, Integer> extractNextString(String raw, int offset) {
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

    public static int findKeyValueDelimiter(String raw, int offset) {
        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ':') {
                return i;
            }
        }
        return raw.length();
    }

    public static boolean hasValueKeyDelimiter(String raw, int offset) {
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

    public static Tupel<String, Integer> extractNextJson(String raw, int offset) {
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
}
