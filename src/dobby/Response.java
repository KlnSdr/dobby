package dobby;

import dobby.filter.FilterManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Response {
    private final HashMap<String, String> headers = new HashMap<>();
    private ResponseCodes code = ResponseCodes.OK;
    private String body = "";

    private final Socket client;
    private final OutputStream out;

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
}
