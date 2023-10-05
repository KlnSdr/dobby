package dobby.files.service;

import dobby.Dobby;
import dobby.files.StaticFile;
import dobby.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try {
            file.setContent(stream.readAllBytes());
        } catch (IOException e) {
            return null;
        }

        files.put(path, file);

        return file;
    }

    private String determineContentType(String path) {
        String[] split = path.split("\\.");
        String extension = split[split.length - 1];
        return ContentType.get(extension);
    }
}
