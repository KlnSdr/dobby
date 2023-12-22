package dobby.files.service;

import dobby.Dobby;
import dobby.files.StaticFile;
import dobby.util.Config;
import dobby.util.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for serving static files
 */
public class StaticFileService {
    private static final Logger LOGGER = new Logger(StaticFileService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static StaticFileService instance;
    private final HashMap<String, StaticFile> files = new HashMap<>();
    private String staticContentPath;
    private int maxFileAge;

    private StaticFileService() {
        Config config = Config.getInstance();

        if (config.getBoolean("dobby.staticContent.disable")) {
            return;
        }
        maxFileAge = config.getInt("dobby.staticContent.maxFileAge", 5);
        staticContentPath = config.getString("dobby.staticContent.directory");
        final int cleanupInterval = config.getInt("dobby.staticContent.cleanUpInterval", 30);

        LOGGER.info("starting static file cleanup scheduler with interval of " + cleanupInterval + " min...");
        scheduler.scheduleAtFixedRate(this::cleanUpStaticFiles, 0, cleanupInterval, TimeUnit.MINUTES);
    }

    public static StaticFileService getInstance() {
        if (instance == null) {
            instance = new StaticFileService();
        }
        return instance;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void cleanUpStaticFiles() {
        LOGGER.info("Cleaning up static files...");
        long currentTime = getCurrentTime();
        files.forEach((path, file) -> {
            if (currentTime - file.getLastAccessed() > (long) maxFileAge * 60 * 60 * 1000) {
                files.remove(path);
            }
        });
    }

    /**
     * Get a static file
     *
     * @param path path to file
     * @return static file
     */
    public StaticFile get(String path) {
        final StaticFile file;
        if (!files.containsKey(path)) {
            file = lookUpFile(path);
        } else {
            file = files.get(path);
        }

        if (file != null) {
            file.setLastAccessed(getCurrentTime());
        }

        return file;
    }

    /**
     * Look up a file in the resources folder
     *
     * @param path path to file
     * @return static file
     */
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

    public void stopScheduler() {
        scheduler.shutdown();
    }
}
