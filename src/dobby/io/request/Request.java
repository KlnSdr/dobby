package dobby.io.request;

import dobby.Config;
import dobby.cookie.Cookie;
import dobby.exceptions.MalformedJsonException;
import dobby.util.json.NewJson;
import common.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Request {
    private static final Logger LOGGER = new Logger(Request.class);
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();
    private RequestTypes type;
    private String path;
    private String rawBody;
    private NewJson body;
    private Map<String, String> headers;
    private Map<String, List<String>> query;

    /**
     * Parses a request from an input stream
     *
     * @param in The input stream to parse the request from
     * @return The parsed request
     */
    public static Request parse(BufferedReader in) throws MalformedJsonException {
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

        if (req.getType() == RequestTypes.POST || req.getType() == RequestTypes.PUT) {
            req.setRawBody(extractBody(in, contentLength));

            final String contentTypeHeader = req.getHeader("Content-Type");
            if (contentTypeHeader != null && contentTypeHeader.contains("application/json")) {
                req.setBody(NewJson.parse(req.getRawBody()));
            } else if (contentTypeHeader != null && contentTypeHeader.contains("multipart/form-data")) {
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

    private static String extractBody(BufferedReader in, int length) {
        StringBuilder body = new StringBuilder();
        int bytesRead = 0;
        while (true) {
            try {
                if (!in.ready() || bytesRead >= length) break;
                body.append((char) in.read());
                bytesRead++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return body.toString();
    }

    private static void parseMultipartForm(Request req) {
        // todo handle malformed multipart form data
        final String boundary = req.getHeader("Content-Type").split("boundary=")[1].split(";")[0];
        final String[] parts = req.getRawBody().split("--" + boundary);

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty() || part.equals("--")) continue;

            final String[] sections = part.split("\\r?\\n\\r?\\n", 2); // split headers from body
            if (sections.length < 2) continue;

            final String headersSection = sections[0];
            final String bodySection = sections[1].trim();
            final String[] headers = headersSection.split("\\r?\\n");
            if (headers.length < 1) continue;

            final String contentDisposition = headers[0];
            if (!contentDisposition.startsWith("Content-Disposition: form-data;")) continue;

            final String name = contentDisposition.split("name=\"")[1].split("\"")[0];
            String filename = null;
            if (contentDisposition.contains("filename=\"")) {
                filename = contentDisposition.split("filename=\"")[1].split("\"")[0];
            }

            if (!name.equals("file") || filename == null) continue;

            final String contentType = headers[1];
            if (!contentType.startsWith("Content-Type: ")) continue;
            final String type = contentType.split("Content-Type: ")[1].trim();
            if (type.isEmpty()) continue;

            byte[] md5Hash = null;

            try {
                md5Hash = MessageDigest.getInstance("MD5").digest(bodySection.getBytes());
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("MD5 algorithm not found");
                LOGGER.trace(e);
            }

            if (md5Hash == null) continue;

            final StringBuilder sb = new StringBuilder();
            for (byte b : md5Hash) {
                sb.append(String.format("%02x", b));
            }
            final String hash = sb.toString();

            final File file = new File(Config.getInstance().getString("dobby.tmpUploadDir", "/tmp") + "/" + hash);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    LOGGER.error("Failed to create file: " + file.getAbsolutePath());
                    LOGGER.trace(e);
                }
            }

            try {
                Files.write(file.toPath(), bodySection.getBytes());
            } catch (IOException e) {
                LOGGER.error("Failed to write file: " + file.getAbsolutePath());
                LOGGER.trace(e);
            }

            req.addFile(name, file);
        }

        req.setRawBody(null);
    }

    private static String extractPath(String line) {
        String[] parts = line.split(" ");
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private static ArrayList<String> consumeInputStream(BufferedReader input) {
        final ArrayList<String> lines = new ArrayList<>();
        String line;
        try {
            while (!(line = input.readLine()).isEmpty()) {
                lines.add(line);
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
        return rawBody;
    }

    private void setRawBody(String rawBody) {
        this.rawBody = rawBody;
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
