package dobby.io.request;

import dobby.cookie.Cookie;
import dobby.util.Json;
import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Request {
    private static final Logger LOGGER = new Logger(Request.class);
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private RequestTypes type;
    private String path;
    private String rawBody;
    private Json body;
    private Map<String, String> headers;
    private Map<String, List<String>> query;

    /**
     * Parses a request from an input stream
     *
     * @param in The input stream to parse the request from
     * @return The parsed request
     */
    public static Request parse(BufferedReader in) {
        Request req = new Request();

        ArrayList<String> lines = consumeInputStream(in);

        String method = extractMethodString(lines.get(0));
        req.setPath(extractPath(lines.get(0)));
        req.setQuery(extractQuery(req.getPath()));
        req.setPath(req.getPath().split("\\?")[0]);
        req.setHeaders(req.extractHeaders(lines));
        req.extractCookies();

        req.setType(RequestTypes.fromString(method));

        if (req.getType() == RequestTypes.POST || req.getType() == RequestTypes.PUT) {
            int contentLength = Integer.parseInt(req.getHeader("Content-Length")); // todo catch exception
            req.setRawBody(extractBody(in, contentLength));
            req.setBody(Json.parse(req.getRawBody()));
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

    private static String extractPath(String line) {
        String[] parts = line.split(" ");
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private static ArrayList<String> consumeInputStream(BufferedReader input) {
        ArrayList<String> lines = new ArrayList<>();
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

    private Map<String, String> extractHeaders(ArrayList<String> lines) {
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
        return headers.get(key);
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
    }

    /**
     * Sets a header of the request
     *
     * @param key   The key of the header
     * @param value The value of the header
     */
    public void setHeader(String key, String value) {
        headers.put(key, value);
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
    public Json getBody() {
        return body;
    }

    private void setBody(Json body) {
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
}
