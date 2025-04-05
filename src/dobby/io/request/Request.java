package dobby.io.request;

import dobby.Config;
import dobby.cookie.Cookie;
import dobby.exceptions.MalformedJsonException;
import dobby.exceptions.RequestTooBigException;
import dobby.util.json.NewJson;
import common.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static dobby.Dobby.DEFAULT_MAX_REQUEST_SIZE;

public class Request {
    private static final Logger LOGGER = new Logger(Request.class);
    private static final int MAX_REQUEST_SIZE = Config.getInstance().getInt("dobby.maxRequestSize", DEFAULT_MAX_REQUEST_SIZE);
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();
    private RequestTypes type;
    private String path;
    private byte[] rawBody;
    private NewJson body;
    private Map<String, String> headers;
    private Map<String, List<String>> query;

    /**
     * Parses a request from an input stream
     *
     * @param in The input stream to parse the request from
     * @return The parsed request
     */
    public static Request parse(InputStream in) throws MalformedJsonException {
        final Request req = new Request();

        final List<String> lines = consumeInputStream(in);

        final String method = extractMethodString(lines.get(0));
        req.setPath(extractPath(lines.get(0)));
        req.setQuery(extractQuery(req.getPath()));
        req.setPath(req.getPath().split("\\?")[0]);
        req.setHeaders(req.extractHeaders(lines));
        req.extractCookies();

        req.setType(RequestTypes.fromString(method));

        int contentLength = 0;
        if (req.getHeader("Content-Length") != null) {
            try {
                contentLength = Integer.parseInt(req.getHeader("Content-Length"));
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid Content-Length header: " + req.getHeader("Content-Length"));
                LOGGER.trace(e);
            }
        }

        if (contentLength > MAX_REQUEST_SIZE) {
            LOGGER.error("Request body too large: " + contentLength + " bytes");
            throw new RequestTooBigException("Request body too large: " + contentLength + " bytes");
        }

        if (req.getType() == RequestTypes.POST || req.getType() == RequestTypes.PUT) {
            final String contentTypeHeader = req.getHeader("Content-Type");
            if (contentTypeHeader != null && contentTypeHeader.contains("application/json")) {
                req.setRawBody(extractBodyLines(in, contentLength));
                req.setBody(NewJson.parse(req.getRawBody()));
            } else if (contentTypeHeader != null && contentTypeHeader.contains("multipart/form-data")) {
                req.setRawBody(extractBodyBytes(in, contentLength));
                parseMultipartForm(req);
            }
        }
        return req;
    }

    private static Map<String, List<String>> extractQuery(String path) {
        HashMap<String, List<String>> queryMap = new HashMap<>();
        String[] parts = path.split("\\?");
        if (parts.length <= 1) {
            return queryMap;
        }
        String[] queries = parts[1].split("&");
        for (String query : queries) {
            String[] queryParts = query.split("=");
            if (queryParts.length <= 1) {
                continue;
            }
            String key = queryParts[0];
            String value = queryParts[1];
            if (queryMap.containsKey(key)) {
                queryMap.get(key).add(value);
            } else {
                ArrayList<String> values = new ArrayList<>();
                values.add(value);
                queryMap.put(key, values);
            }
        }
        return queryMap;
    }

    private static String extractBodyLines(InputStream in, int length) {
        StringBuilder body = new StringBuilder();
        final BufferedReader input = new BufferedReader(new InputStreamReader(in));
        int bytesRead = 0;
        while (true) {
            try {
                if (!input.ready() || bytesRead >= length) break;
                body.append((char) input.read());
                bytesRead++;
                if (bytesRead >= MAX_REQUEST_SIZE) {
                    throw new RequestTooBigException("Request body too large: " + bytesRead + " bytes");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return body.toString();
    }

    private static byte[] extractBodyBytes(InputStream in, int length) {
        final byte[] body = new byte[length];
        int bytesRead = 0;
        while (true) {
            try {
                if (bytesRead >= length) break;
                int read = in.read(body, bytesRead, length - bytesRead);
                if (read == -1) break;
                bytesRead += read;
                if (bytesRead >= MAX_REQUEST_SIZE) {
                    throw new RequestTooBigException("Request body too large: " + bytesRead + " bytes");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return body;
    }

    private static void parseMultipartForm(Request req) {
        req.setBody(new NewJson());
        final String boundary = req.getHeader("Content-Type").split("boundary=")[1].split(";")[0];
        final byte[] body = req.getRawBodyBytes();
        final byte[] boundaryBytes = ("--" + boundary).getBytes();
        final byte[] endBoundaryBytes = ("--" + boundary + "--").getBytes();

        int pos = 0;

        while (pos < body.length) {
            int start = indexOf(body, boundaryBytes, pos);
            if (start == -1) break;
            start += boundaryBytes.length;

            if (body[start] == '\r' && body[start + 1] == '\n') {
                start += 2;
            }

            int headerEnd = indexOf(body, "\r\n\r\n".getBytes(), start);
            if (headerEnd == -1) break;

            String headers = new String(body, start, headerEnd - start);
            int contentStart = headerEnd + 4;

            int nextBoundary = indexOf(body, boundaryBytes, contentStart);
            if (nextBoundary == -1) {
                nextBoundary = indexOf(body, endBoundaryBytes, contentStart);
                if (nextBoundary == -1) nextBoundary = body.length;
            }

            byte[] content = Arrays.copyOfRange(body, contentStart, nextBoundary - 2); // -2 to trim last \r\n

            String name = null;
            String filename = null;
            for (String line : headers.split("\r\n")) {
                if (line.toLowerCase().startsWith("content-disposition")) {
                    name = extractField(line, "name");
                    filename = extractField(line, "filename");
                }
            }

            if (name == null) continue;

            if (filename != null) {
                final File tmpFile = saveFile(content);
                if (tmpFile != null) {
                    req.addFile(name, tmpFile);
                    LOGGER.debug("File uploaded: " + filename + " -> " + tmpFile.getAbsolutePath());
                } else {
                    LOGGER.error("Failed to save file: " + filename);
                }
            } else {
                final String value = new String(content);
                req.getBody().setString(name, value);
            }

            pos = nextBoundary;
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle, int start) {
        outer:
        for (int i = start; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static String extractField(String contentDisposition, String fieldName) {
        final String pattern = fieldName + "=\"";
        int start = contentDisposition.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        final int end = contentDisposition.indexOf("\"", start);
        if (end == -1) return null;
        return contentDisposition.substring(start, end);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File saveFile(byte[] data) {
        final String tmpFileName = UUID.randomUUID().toString();

        final File file = new File(Config.getInstance().getString("dobby.tmpUploadDir", "/tmp") + "/" + tmpFileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Failed to create file: " + file.getAbsolutePath());
                LOGGER.trace(e);
                return null;
            }
        }

        try {
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            LOGGER.error("Failed to write file: " + file.getAbsolutePath());
            LOGGER.trace(e);
            return null;
        }

        return file;
    }

    private static String extractPath(String line) {
        String[] parts = line.split(" ");
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private static ArrayList<String> consumeInputStream(InputStream in) {
        final ArrayList<String> lines = new ArrayList<>();
        StringBuilder lineBuffer = new StringBuilder();
        try {
            while (true) {
                final int read = in.read();
                if (read == -1) break;
                final char c = (char) read;
                if (c == '\n') {
                    lines.add(lineBuffer.toString());
                    if (lineBuffer.length() == 0) {
                        break;
                    }
                    lineBuffer = new StringBuilder();
                } else if (c != '\r') {
                    lineBuffer.append(c);
                }
            }
            if (lineBuffer.length() > 0) {
                lines.add(lineBuffer.toString());
            }
            return lines;
        } catch (Exception e) {
            LOGGER.trace(e);
            return new ArrayList<>();
        }
    }

    private static String extractMethodString(String line) {
        String[] parts = line.split(" ");
        if (parts.length > 0) {
            return parts[0].toUpperCase();
        }
        return "";
    }

    private Map<String, String> extractHeaders(List<String> lines) {
        HashMap<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(": ");
            if (parts.length > 1) {
                headers.put(parts[0], parts[1]);
            }
        }
        return headers;
    }

    /**
     * Gets the type of the request
     *
     * @return The type of the request
     */
    public RequestTypes getType() {
        return type;
    }

    private void setType(RequestTypes type) {
        this.type = type;
    }

    /**
     * Gets the path of the request
     *
     * @return The path of the request
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the headers of the request
     *
     * @return The header key of the request
     */
    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }

    /**
     * Gets the header keys of the request
     *
     * @return A set containing all header keys
     */
    public Set<String> getHeaderKeys() {
        return headers.keySet();
    }

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;

        final Map<String, String> newHeaders = new HashMap<>();
        for (String key : headers.keySet()) {
            newHeaders.put(key.toLowerCase(), headers.get(key));
        }
        this.headers = newHeaders;
    }

    /**
     * Sets a header of the request
     *
     * @param key   The key of the header
     * @param value The value of the header
     */
    public void setHeader(String key, String value) {
        headers.put(key.toLowerCase(), value);
    }

    /**
     * Gets the query of the request
     *
     * @return The query of the request
     */
    public List<String> getQuery(String key) {
        return query.get(key);
    }

    /**
     * Gets the query keys of the request
     *
     * @return A set containing all query keys
     */
    public Set<String> getQueryKeys() {
        return query.keySet();
    }

    private void setQuery(Map<String, List<String>> query) {
        this.query = query;
    }

    /**
     * Sets a query of the request
     *
     * @param key   The key of the query
     * @param value The value of the query
     */
    public void setQuery(String key, String value) {
        if (!query.containsKey(key)) {
            query.put(key, new ArrayList<>());
        }

        query.get(key).add(value);
    }

    /**
     * Gets the string representation of the body of the request
     *
     * @return The body of the request as a string
     */
    public String getRawBody() {
        return new String(rawBody);
    }

    public byte[] getRawBodyBytes() {
        return rawBody;
    }

    private void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }

    private void setRawBody(String rawBody) {
        this.rawBody = rawBody.getBytes();
    }

    /**
     * Gets the body of the request
     *
     * @return The body of the request
     */
    public NewJson getBody() {
        return body;
    }

    private void setBody(NewJson body) {
        this.body = body;
    }

    private void setCookie(String key, String value) {
        cookies.put(key, new Cookie(key, value));
    }

    /**
     * Gets the cookies of the request
     *
     * @return The cookies of the request
     */
    public Cookie getCookie(String key) {
        return cookies.get(key);
    }

    private void extractCookies() {
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader == null) {
            return;
        }
        String[] cookies = cookieHeader.split("; ");
        for (String cookie : cookies) {
            String[] parts = cookie.split("=");
            if (parts.length > 1) {
                setCookie(parts[0], parts[1]);
            }
        }
    }

    /**
     * Sets a path parameter of the request
     *
     * @param pathParams The path parameters
     */
    public void setPathParams(HashMap<String, String> pathParams) {
        this.pathParams.putAll(pathParams);
    }

    /**
     * Gets the path parameter of the request
     *
     * @param key The key of the path parameter
     * @return The path parameter of the request
     */
    public String getParam(String key) {
        return pathParams.get(key);
    }

    private void addFile(String name, File file) {
        files.put(name, file);
    }

    public Map<String, File> getFiles() {
        return files;
    }

    public File getFile(String name) {
        return files.get(name);
    }
}
