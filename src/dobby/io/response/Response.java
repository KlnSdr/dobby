package dobby.io.response;

import dobby.cookie.Cookie;
import dobby.filter.FilterManager;
import dobby.io.HttpContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Response class
 */
public class Response {
    private final HashMap<String, List<String>> headers = new HashMap<>();
    private final Socket client;
    private final OutputStream out;
    private final HashMap<String, Cookie> cookies = new HashMap<>();
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
     *
     * @param code Response code
     */
    public void setCode(ResponseCodes code) {
        this.code = code;
    }

    /**
     * Set response body
     *
     * @param body Response body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Set response header
     *
     * @param key   Header key
     * @param value Header value
     */
    public void setHeader(String key, String value) {
        List<String> header = headers.getOrDefault(key, new ArrayList<>());
        header.add(value);
        headers.put(key, header);
    }

    private void calculateContentLength() {
        int length = body.getBytes().length;
        setHeader("Content-Length", Integer.toString(length));
    }

    private byte[] build() {
        calculateContentLength();
        StringBuilder builder = new StringBuilder("HTTP/1.1 ");

        builder.append(code.getCode());
        builder.append(" ");
        builder.append(code.getMessage());
        builder.append("\r\n");

        for (String key : headers.keySet()) {
            builder.append(buildHeader(key));
        }

        builder.append("\r\n");
        builder.append(body);

        return builder.toString().getBytes();
    }

    private String buildHeader(String key) {
        StringBuilder builder = new StringBuilder();

        for (String value : headers.get(key)) {
            builder.append(key);
            builder.append(": ");
            builder.append(value);
            builder.append("\r\n");
        }

        return builder.toString();
    }

    /**
     * Send response
     *
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
     *
     * @return The response cookies
     */
    public HashMap<String, Cookie> getCookies() {
        return cookies;
    }

    /**
     * Set a cookie
     *
     * @param key   The key of the cookie
     * @param value The value of the cookie
     */
    public void setCookie(String key, Cookie value) {
        cookies.put(key, value);
    }
}
