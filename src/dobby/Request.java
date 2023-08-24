package dobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Request {
    private RequestTypes type;
    private String path;
    private String rawBody;
    private Json body;
    private Map<String, String> headers;
    private Map<String, List<String>> query;

    public static Request parse(BufferedReader in) {
        Request req = new Request();

        ArrayList<String> lines = consumeInputStream(in);

        String method = extractMethodString(lines.get(0));
        req.setPath(extractPath(lines.get(0)));
        req.setQuery(extractQuery(req.getPath()));
        req.setPath(req.getPath().split("\\?")[0]);
        req.setHeaders(req.extractHeaders(lines));

        req.setType(RequestTypes.fromString(method));

        if (req.getType() == RequestTypes.POST) {
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
            e.printStackTrace();
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

    public RequestTypes getType() {
        return type;
    }

    private void setType(RequestTypes type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Set<String> getHeaderKeys() {
        return headers.keySet();
    }

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getQuery(String key) {
        return query.get(key);
    }

    public Set<String> getQueryKeys() {
        return query.keySet();
    }

    private void setQuery(Map<String, List<String>> query) {
        this.query = query;
    }

    public String getRawBody() {
        return rawBody;
    }

    private void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    private void setBody(Json body) {
        this.body = body;
    }

    private Json getBody() {
        return body;
    }
}
