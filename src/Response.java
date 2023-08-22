import java.util.HashMap;

public class Response {
    private final HashMap<String, String> headers = new HashMap<>();
    private ResponseCodes code = ResponseCodes.OK;
    private String body = "";

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

    public byte[] build() {
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
}
