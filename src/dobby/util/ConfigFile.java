package dobby.util;

import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ConfigFile {
    private static ConfigFile instance;
    private final Logger LOGGER = new Logger(ConfigFile.class);
    private Json configJson;

    private ConfigFile() {
    }

    public static ConfigFile getInstance() {
        if (instance == null) {
            instance = new ConfigFile();
        }
        return instance;
    }

    public void loadConfig(Class<?> applicationClass) {
        InputStream stream = applicationClass.getResourceAsStream("resource/application.json");
        String rawConfig = loadFileContent(stream);

        configJson = Json.parse(rawConfig);
    }

    private String loadFileContent(InputStream stream) {
        if (stream == null) {
            LOGGER.error("application.json not found");
            System.exit(1);
        }

        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(stream));
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
