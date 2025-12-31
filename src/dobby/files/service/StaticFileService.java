package dobby.files.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.Dobby;
import dobby.IConfig;
import dobby.files.StaticFile;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observable;
import dobby.observer.Observer;
import dobby.task.ISchedulerService;
import dobby.task.SchedulerService;
import dobby.Config;
import dobby.util.Tupel;
import common.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for serving static files
 */
@RegisterFor(IStaticFileService.class)
public class StaticFileService implements Observable<Tupel<String, StaticFile>>, IStaticFileService {
    private static final Logger LOGGER = new Logger(StaticFileService.class);
    private final HashMap<String, StaticFile> files = new HashMap<>();
    private Tupel<Class<?>, String>[] staticContentPath;
    private int maxFileAge;
    private final IExternalDocRootService externalDocRootService;
    private final ISchedulerService schedulerService;
    private final IConfig config;

    @Inject
    public StaticFileService(ISchedulerService schedulerService, IExternalDocRootService externalDocRootService, IConfig config) {
        this.externalDocRootService = externalDocRootService;
        this.schedulerService = schedulerService;
        this.config = config;
    }

    public void init() {
        if (config.getBoolean("dobby.staticContent.disable")) {
            return;
        }
        maxFileAge = config.getInt("dobby.staticContent.maxFileAge", 5);
        staticContentPath = getAllStaticContentPaths(config.getString("dobby.staticContent.directory"));
        final int cleanupInterval = config.getInt("dobby.staticContent.cleanUpInterval", 30);

        LOGGER.info("starting static file cleanup scheduler with interval of " + cleanupInterval + " min...");
        schedulerService.addRepeating(this::cleanUpStaticFiles, cleanupInterval, TimeUnit.MINUTES);
    }

    @SuppressWarnings("unchecked")
    private static Tupel<Class<?>, String>[] getAllStaticContentPaths(String configPath) {
        String[] paths = configPath.split(",");
        ArrayList<Tupel<Class<?>, String>> staticContentPaths = new ArrayList<>();

        for (String path: paths) {
            final String[] split = path.split("\\\\"); // soo geistlos!
            final Class<?> baseClass;
            final String resourcePath;

            if (split.length != 2) {
                baseClass = Dobby.getMainClass();
                resourcePath = path;

                staticContentPaths.add(new Tupel<>(baseClass, resourcePath));
                continue;
            }

            try {
                baseClass = Class.forName(split[0]);
                resourcePath = split[1];
                staticContentPaths.add(new Tupel<>(baseClass, resourcePath));
            } catch (ClassNotFoundException e) {
                LOGGER.error("Class not found: " + split[0]);
                LOGGER.trace(e);
            }
        }
        return staticContentPaths.toArray(new Tupel[0]);
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void cleanUpStaticFiles() {
        LOGGER.info("Cleaning up static files...");
        long currentTime = getCurrentTime();
        files.forEach((path, file) -> {
            if (currentTime - file.getLastAccessed() > (long) maxFileAge * 60 * 60 * 1000) {
                deleteFile(path);
            }
        });
    }

    public void storeFile(String path, StaticFile file) {
        storeFileNoEvent(path, file);
        fireEvent(createEvent(path, file));
    }

    public void storeFileNoEvent(String path, StaticFile file) {
        files.put(path, file);
    }

    public void deleteFile(String path) {
        StaticFile file = files.remove(path);
        if (file != null) {
            fireEvent(deleteEvent(path, file));
        }
    }

    /**
     * Get a static file
     *
     * @param path path to file
     * @return static file
     */
    public StaticFile get(String path) {
        StaticFile file;

        boolean fileNewlyAdded = false;
        if (!files.containsKey(path)) {
            file = externalDocRootService.get(path);
            if (file == null) {
                file = lookUpFile(path);
            }
            fileNewlyAdded = true;
        } else {
            file = files.get(path);
        }

        if (file != null) {
            file.setLastAccessed(getCurrentTime());
            storeFileNoEvent(path, file);

            if (fileNewlyAdded) {
                fireEvent(createEvent(path, file));
            } else {
                fireEvent(modifyEvent(path, file));
            }

            // read file from cache again to ensure that changes made by observers are applied
            file = this.files.get(path);
        }

        return file;
    }

    /**
     * Look up a file in the resources folder<br>
     * If the file is found, it is added to the files map<br>
     * If the file is not found, null is returned<br>
     * All static content paths are tried in order<br>
     *
     * @param path path to file
     * @return static file
     */
    private StaticFile lookUpFile(String path) {
        InputStream stream;
        int i = 0;
        StaticFile file = null;

        do {
            stream = staticContentPath[i]._1().getResourceAsStream("resource/" + staticContentPath[i]._2() + path);
            i++;

            if (stream == null) {
                continue;
            }

            file = new StaticFile();
            file.setContentType(determineContentType(path));

            try {
                file.setContent(stream.readAllBytes());
                storeFile(path, file);
                break;
            } catch (IOException e) {
                LOGGER.error("Error reading file: " + path);
                LOGGER.trace(e);
                file = null;
            }
        } while (stream == null && i < staticContentPath.length);

        return file;
    }

    public static String determineContentType(String path) {
        String[] split = path.split("\\.");
        String extension = split[split.length - 1];
        return ContentType.get(extension);
    }

    private final ArrayList<Observer<Tupel<String, StaticFile>>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<Tupel<String, StaticFile>> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Tupel<String, StaticFile>> observer) {
        observers.remove(observer);
    }

    @Override
    public void fireEvent(Event<Tupel<String, StaticFile>> event) {
        observers.forEach(observer -> observer.onEvent(event));
    }

    private Event<Tupel<String, StaticFile>> createEvent(String path, StaticFile file) {
        return new Event<>(EventType.CREATED, new Tupel<>(path, file));
    }

    private Event<Tupel<String, StaticFile>> deleteEvent(String path, StaticFile file) {
        return new Event<>(EventType.DELETED, new Tupel<>(path, file));
    }

    private Event<Tupel<String, StaticFile>> modifyEvent(String path, StaticFile file) {
        return new Event<>(EventType.MODIFIED, new Tupel<>(path, file));
    }
}
