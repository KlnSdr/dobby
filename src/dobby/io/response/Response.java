package dobby.io.response;

import dobby.filter.FilterManager;
import dobby.io.request.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Response {
    private final HashMap<String, String> headers = new HashMap<>();
    private final Socket client;
    private final OutputStream out;
    private final HashMap<String, String> cookies = new HashMap<>();
    private ResponseCodes code = ResponseCodes.OK;
    private String body = "";
    private Request request;

    public Response(Socket client) throws IOException {
        this.client = client;
        out = client.getOutputStream();
    }

    public void setCode(ResponseCodes code) {
        this.code = code;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    private void calculateContentLength() {
        int length = body.getBytes().length;
        headers.put("Content-Length", Integer.toString(length));
    }

    private byte[] build() {
        calculateContentLength();
        StringBuilder builder = new StringBuilder("HTTP/1.1 ");

        builder.append(code.getCode());
        builder.append(" ");
        builder.append(code.getMessage());
        builder.append("\r\n");

        for (String key : headers.keySet()) {
            builder.append(key);
            builder.append(": ");
            builder.append(headers.get(key));
            builder.append("\r\n");
        }

        builder.append("\r\n");
        builder.append(body);

        return builder.toString().getBytes();
    }

    public void send() throws IOException {
        FilterManager.getInstance().runPostFilters(this);
        out.write(build());
        client.close();
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }

    public void setCookie(String key, String value) {
        cookies.put(key, value);
    }

    public void setCookie(String key, String value, int maxAge) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; Path=/");
    }

    public void setCookie(String key, String value, String path) {
        cookies.put(key, value + "; Path=" + path);
    }

    public void setCookie(String key, String value, int maxAge, boolean httpOnly) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; HttpOnly");
    }

    public void setCookie(String key, String value, int maxAge, boolean httpOnly, boolean secure) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; HttpOnly; Secure");
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
