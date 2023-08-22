import java.io.BufferedReader;
import java.util.ArrayList;

public class Request {
    private RequestTypes type;

    public static Request parse(BufferedReader in) {
        Request req = new Request();

        ArrayList<String> lines = consumeInputStream(in);

        String method = extractMethodString(lines.get(0));

        if (method.equals("GET")) {
            req.setType(RequestTypes.GET);
        } else {
            req.setType(RequestTypes.UNKNOWN);
        }

        return req;
    }

    private static ArrayList<String> consumeInputStream(BufferedReader input) {
        ArrayList<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = input.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
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

    public RequestTypes getType() {
        return type;
    }

    private void setType(RequestTypes type) {
        this.type = type;
    }
}
