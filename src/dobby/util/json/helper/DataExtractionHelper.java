package dobby.util.json.helper;

import dobby.exceptions.MalformedJsonException;
import dobby.util.Tupel;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataExtractionHelper {
    private static final Logger LOGGER = new Logger(DataExtractionHelper.class);
    private static final String[] VALUE_DELIMITERS = new String[]{"{", "}", "[", "]", ":", "\""};

    public static Tupel<Boolean, Integer> extractNextBoolean(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ',' || Arrays.stream(VALUE_DELIMITERS).anyMatch(d -> d.equals(String.valueOf(c)))) {
                final String value = sb.toString().trim();
                if (value.equals("true")) {
                    return new Tupel<>(true, i);
                } else if (value.equals("false")) {
                    return new Tupel<>(false, i);
                } else {
                    return null;
                }
            }
            sb.append(c);
        }

        final String value = sb.toString().trim();
        if (value.equals("true")) {
            return new Tupel<>(true, raw.length());
        } else if (value.equals("false")) {
            return new Tupel<>(false, raw.length());
        } else {
            return null;
        }
    }

    public static Tupel<String, Integer> extractNextNumber(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == ',' || Arrays.stream(VALUE_DELIMITERS).anyMatch(d -> d.equals(String.valueOf(c)))) {
                return new Tupel<>(sb.toString().trim(), i);
            }
            sb.append(c);
        }
        return new Tupel<>(sb.toString().trim(), raw.length());
    }

    public static Tupel<String, Integer> extractNextString(String raw, int offset) {
        final StringBuilder sb = new StringBuilder();

        for (int i = offset + 1; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == '"') {
                return new Tupel<>(sb.toString(), i);
            }
            if (c == '\\') {
                final Character next = checkIfEscapingNext(raw, i);
                if (next != null) {
                    sb.append(next);
                    i++;
                }
                continue;
            }
            sb.append(c);
        }
        return null;
    }

    private static Character checkIfEscapingNext(String raw, int position) {
        if (position + 1 <= raw.length() - 1) {
            final char next = raw.charAt(position + 1);

            if (next == '"' || next == '\\') {
                return next;
            }
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
        return extractNextEnclosedType(raw, offset, '{', '}');
    }

    public static Tupel<String, Integer> extractNextArray(String raw, int offset) {
        return extractNextEnclosedType(raw, offset, '[', ']');
    }

    private static Tupel<String, Integer> extractNextEnclosedType(String raw, int offset, char start, char end) {
        final StringBuilder sb = new StringBuilder();
        int depth = 0;

        for (int i = offset + 1; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == start) {
                depth++;
            } else if (c == end) {
                if (depth == 0) {
                    return new Tupel<>(sb.toString(), i);
                }
                depth--;
            }
            sb.append(c);
        }
        return null;
    }

    public static List<Object> extractArrayData(String raw) throws MalformedJsonException {
        List<Object> resultList = new ArrayList<>();
        boolean isFirstElement = true;

        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);

            if (c == '{') {
                final Tupel<String, Integer> res = extractNextJson(raw, i);

                if (res == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (isFirstElement) {
                    isFirstElement = false;
                } else if(!hasValueKeyDelimiter(raw, i)) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                resultList.add(NewJson.parse(res._1()));
                i = res._2();
            } else if (c == '[') {
                final Tupel<String, Integer> res = extractNextArray(raw, i);

                if (res == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (isFirstElement) {
                    isFirstElement = false;
                } else if(!hasValueKeyDelimiter(raw, i)) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                resultList.add(extractArrayData(res._1()));
                i = res._2();
            } else if (c == '"') {
                final Tupel<String, Integer> res = extractNextString(raw, i);

                if (res == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (isFirstElement) {
                    isFirstElement = false;
                } else if(!hasValueKeyDelimiter(raw, i)) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                resultList.add(res._1());
                i = res._2();
            } else if (c == 't' || c == 'f') {
                final Tupel<Boolean, Integer> res = extractNextBoolean(raw, i);

                if (res == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (isFirstElement) {
                    isFirstElement = false;
                } else if(!hasValueKeyDelimiter(raw, i)) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                resultList.add(res._1());
                i = res._2();
            } else if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
                final Tupel<String, Integer> res = extractNextNumber(raw, i);

                if (res == null) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (isFirstElement) {
                    isFirstElement = false;
                } else if(!hasValueKeyDelimiter(raw, i)) {
                    LOGGER.error("Malformed JSON: " + raw);
                    if (!NewJson.isSilentExceptions()) {
                        throw new MalformedJsonException(raw);
                    }
                    return null;
                }

                if (res._1().contains(".")) {
                    resultList.add(Double.parseDouble(res._1()));
                } else {
                    resultList.add(Integer.parseInt(res._1()));
                }

                i = res._2();
            }
        }

        return resultList;
    }
}
