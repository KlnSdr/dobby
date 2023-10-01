package dobby.io.response;

import dobby.filter.FilterManager;
import dobby.io.HttpContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * Response class
 */
public class Response {
    private final HashMap<String, String> headers = new HashMap<>();
    private final Socket client;
    private final OutputStream out;
    private final HashMap<String, String> cookies = new HashMap<>();
    private final HttpContext context;
    private ResponseCodes code = ResponseCodes.OK;
    private String body = "";
    private boolean didSend = false;

    /**
     * Constructor
     *
     * @param client  Client socket
     * @param context HttpContext
     * @throws IOException If an I/O error occurs
     */
    public Response(Socket client, HttpContext context) throws IOException {
        this.client = client;
        this.context = context;
        out = client.getOutputStream();
    }

    /**
     * Set response code
     * @param code Response code
     */
    public void setCode(ResponseCodes code) {
        this.code = code;
    }

    /**
     * Set response body
     * @param body Response body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Set response header
     * @param key Header key
     * @param value Header value
     */
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

    /**
     * Send response
     * @throws IOException If an I/O error occurs
     */
    public void send() throws IOException {
        if (didSend) {
            throw new IOException("Response already sent");
        }
        didSend = true;

        out.write(build());
        client.close();
    }

    public boolean didSend() {
        return didSend;
    }

    /**
     * Get the response cookies
     * @return The response cookies
     */
    public HashMap<String, String> getCookies() {
        return cookies;
    }

    /**
     * Set a cookie
     * @param key The key of the cookie
     * @param value The value of the cookie
     */
    public void setCookie(String key, String value) {
        cookies.put(key, value);
    }

    /**
     * Set a cookie
     * @param key The key of the cookie
     * @param value The value of the cookie
     * @param maxAge The max age of the cookie
     */
    public void setCookie(String key, String value, int maxAge) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; Path=/");
    }

    /**
     * Set a cookie
     * @param key The key of the cookie
     * @param value The value of the cookie
     * @param path The path of the cookie
     */
    public void setCookie(String key, String value, String path) {
        cookies.put(key, value + "; Path=" + path);
    }

    /**
     * Set a cookie
     * @param key The key of the cookie
     * @param value The value of the cookie
     * @param maxAge The max age of the cookie
     * @param httpOnly Whether the cookie is http only
     */
    public void setCookie(String key, String value, int maxAge, boolean httpOnly) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; HttpOnly");
    }

    /**
     * Set a cookie
     * @param key The key of the cookie
     * @param value The value of the cookie
     * @param maxAge The max age of the cookie
     * @param httpOnly Whether the cookie is http only
     * @param secure Whether the cookie is secure
     */
    public void setCookie(String key, String value, int maxAge, boolean httpOnly, boolean secure) {
        cookies.put(key, value + "; Max-Age=" + maxAge + "; HttpOnly; Secure");
    }
}
