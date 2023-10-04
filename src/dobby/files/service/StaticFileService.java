package dobby.files.service;

import dobby.Dobby;
import dobby.files.StaticFile;
import dobby.util.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.Collectors;

public class StaticFileService {
    private static StaticFileService instance;
    private final HashMap<String, StaticFile> files = new HashMap<>();
    private String staticContentPath;

    private StaticFileService() {
        Config config = Config.getInstance();

        if (config.getBoolean("disableStaticContent")) {
            return;
        }

        staticContentPath = config.getString("staticContentDir");
    }

    public static StaticFileService getInstance() {
        if (instance == null) {
            instance = new StaticFileService();
        }
        return instance;
    }

    public StaticFile get(String path) {
        if (!files.containsKey(path)) {
            return lookUpFile(path);
        }
        return files.get(path);
    }

    private StaticFile lookUpFile(String path) {
        InputStream stream = Dobby.getMainClass().getResourceAsStream("resource/" + staticContentPath + path);

        if (stream == null) {
            return null;
        }

        StaticFile file = new StaticFile();
        file.setContentType(determineContentType(path));
        file.setContent(loadFileContent(stream));

        files.put(path, file);

        return file;
    }

    private String loadFileContent(InputStream stream) {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private String determineContentType(String path) {
        String[] split = path.split("\\.");
        String extension = split[split.length - 1];

        switch (extension) {
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "pdf":
                return "application/pdf";
            case "zip":
                return "application/zip";
            case "gz":
                return "application/gzip";
            case "mp3":
                return "audio/mpeg";
            case "mp4":
                return "video/mp4";
            case "webm":
                return "video/webm";
            case "ogg":
                return "audio/ogg";
            case "wav":
                return "audio/wav";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
}
