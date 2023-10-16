package dobby.io.response;

import dobby.cookie.Cookie;
import dobby.util.Json;

import java.io.ByteArrayOutputStream;
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
    private ResponseCodes code = ResponseCodes.OK;
    private byte[] body = new byte[0];
    private boolean didSend = false;

    /**
     * Constructor
     *
     * @param client Client socket
     * @throws IOException If an I/O error occurs
     */
    public Response(Socket client) throws IOException {
        this.client = client;
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
        if (body == null) {
            return;
        }
        this.body = body.getBytes();
    }

    /**
     * Set response body as bytes
     *
     * @param body Response body
     */
    public void setBody(byte[] body) {
        if (body == null) {
            return;
        }
        this.body = body;
    }

    /**
     * Set response body as JSON
     *
     * @param body Response body
     */
    public void setBody(Json body) {
        if (body == null) {
            return;
        }
        this.body = body.toString().getBytes();
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
        int length = body.length;
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(builder.toString().getBytes());
            outputStream.write(body);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return buildInternalError();
        }
    }

    private byte[] buildInternalError() {
        return "HTTP/1.1 500 Internal Server Error\r\n".getBytes();
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

    /**
     * Check if the response was sent
     *
     * @return True if the response was sent, false otherwise
     */
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
