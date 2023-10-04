package dobby.util;

import dobby.Dobby;
import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Config {
    private static Config instance;
    private final Logger LOGGER = new Logger(Config.class);
    private Json configJson = new Json();

    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public void loadConfig() {
        InputStream stream = Dobby.getMainClass().getResourceAsStream("resource/application.json");
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

    public int getInt(String key, int defaultValue) {
        return getIntOrDefault(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key, String defaultValue) {
        return getStringOrDefault(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getBooleanOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
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

    private String getStringOrDefault(String key, String defaultValue) {
        String stringValue = configJson.get(key);

        if (stringValue == null) {
            return defaultValue;
        }

        return stringValue;
    }

    private boolean getBooleanOrDefault(String key, boolean defaultValue) {
        String stringValue = configJson.get(key);

        if (stringValue == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(stringValue);
    }
}
