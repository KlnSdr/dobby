package com.klnsdr.dobby.files.service;

import com.klnsdr.dobby.Dobby;
import com.klnsdr.dobby.files.StaticFile;
import com.klnsdr.dobby.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class StaticFileService {
    private static StaticFileService instance;
    private final HashMap<String, StaticFile> files = new HashMap<>();
    private String staticContentPath;

    private StaticFileService() {
        Config config = Config.getInstance();

        if (config.getBoolean("com.klnsdr.dobby.disableStaticContent")) {
            return;
        }

        staticContentPath = config.getString("dobby.staticContentDir");
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
