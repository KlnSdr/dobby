package dobby.util;

import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.stream.Collectors;

public class ConfigFile {
    private final Logger LOGGER = new Logger(ConfigFile.class);
    private final Json configJson;

    public ConfigFile(Class<?> applicationClass) {
        URL configFile = applicationClass.getResource("application.json");
        String rawConfig = loadFileContent(configFile);
        configJson = Json.parse(rawConfig);
    }

    private String loadFileContent(URL filePath) {
        if (filePath == null) {
            LOGGER.error("application.json not found");
            System.exit(1);
        }

        File config = new File(filePath.getFile());
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(config));
        } catch (FileNotFoundException e) {
            LOGGER.trace(e);
            System.exit(1);
            return "";
        }
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public int getPort() {
        return getIntOrDefault("port", 3000);
    }

    public int getThreads() {
        return getIntOrDefault("threads", 10);
    }

    private int getIntOrDefault(String key, int defaultValue) {
        String stringValue = configJson.get(key);

        if (stringValue == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
