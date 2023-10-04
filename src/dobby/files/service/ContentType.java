package dobby.files.service;

import java.util.HashMap;

public class ContentType {
    private static final HashMap<String, String> types = new HashMap<>();

    static {
        types.put("html", "text/html");
        types.put("css", "text/css");
        types.put("js", "text/javascript");
        types.put("json", "application/json");
        types.put("png", "image/png");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("gif", "image/gif");
        types.put("svg", "image/svg+xml");
        types.put("ico", "image/x-icon");
        types.put("mp3", "audio/mpeg");
        types.put("wav", "audio/wav");
        types.put("txt", "text/plain");
        types.put("pdf", "application/pdf");
        types.put("xml", "application/xml");
        types.put("bmp", "image/bmp");
    }

    public static String get(String extension) {
        return types.getOrDefault(extension, "application/octet-stream");
    }

}
