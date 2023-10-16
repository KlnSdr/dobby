package dobby.util;

import dobby.Dobby;
import dobby.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Class for loading the config file
 */
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

    /**
     * Loads the config file
     */
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

    /**
     * Gets the value of the given key as an int
     *
     * @param key          The key to get the value of
     * @param defaultValue The default value to return if the key is not found
     * @return The value of the given key as an int
     */
    public int getInt(String key, int defaultValue) {
        return getIntOrDefault(key, defaultValue);
    }

    /**
     * Gets the value of the given key as an int
     *
     * @param key The key to get the value of
     * @return The value of the given key as an int
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Gets the value of the given key as a string
     *
     * @param key          The key to get the value of
     * @param defaultValue The default value to return if the key is not found
     * @return The value of the given key as a string
     */
    public String getString(String key, String defaultValue) {
        return getStringOrDefault(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a string
     *
     * @param key The key to get the value of
     * @return The value of the given key as a string
     */
    public String getString(String key) {
        return getString(key, "");
    }

    /**
     * Gets the value of the given key as a boolean
     *
     * @param key          The key to get the value of
     * @param defaultValue The default value to return if the key is not found
     * @return The value of the given key as a boolean
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBooleanOrDefault(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a boolean
     *
     * @param key The key to get the value of
     * @return The value of the given key as a boolean
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    private int getIntOrDefault(String key, int defaultValue) {
        Integer intValue = configJson.getInt(key);

        if (intValue == null) {
            return defaultValue;
        }

        return intValue;
    }

    private String getStringOrDefault(String key, String defaultValue) {
        String stringValue = configJson.getString(key);

        if (stringValue == null) {
            return defaultValue;
        }

        return stringValue;
    }

    private boolean getBooleanOrDefault(String key, boolean defaultValue) {
        String stringValue = configJson.getString(key);

        if (stringValue == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(stringValue);
    }
}
