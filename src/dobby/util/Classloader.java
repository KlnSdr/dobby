package dobby.util;

import dobby.logging.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public abstract class Classloader<T> {
    private final Logger LOGGER = new Logger(Classloader.class);
    private static final String[] JarPathBlacklist = {"META-INF"};
    protected String packageName;

    protected Set<Class<? extends T>> loadClasses() {
        Set<Class<? extends T>> clazzes;
        if (isJar()) {
            clazzes = loadClassesFromJar();
        } else {
            clazzes = loadClassesFromDirectory();
        }
        clazzes = clazzes.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        return clazzes;
    }

    public Set<String> getPackages() {
        if (isJar()) {
            return getPackagesFromJar();
        } else {
            return getPackagesFromDirectory();
        }
    }

    private Set<String> getPackagesFromDirectory() {
        InputStream istream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replace(".", "/"));
        if (istream == null) {
            LOGGER.error("Could not load classes. Package '" + packageName + "' not found.");
            return Collections.emptySet();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        return reader.lines().filter(line -> !line.endsWith(".class")).collect(Collectors.toSet());
    }

    private Set<Class<? extends T>> loadClassesFromDirectory() {
        InputStream istream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replace(".", "/"));
        if (istream == null) {
            LOGGER.error("Could not load classes. Package '" + packageName + "' not found.");
            return Collections.emptySet();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));

        return reader.lines().filter(line -> line.endsWith(".class")).map(this::filterClasses).collect(Collectors.toSet());
    }

    private Set<String> getPackagesFromJar() {
        try (JarFile jar =
                     new JarFile(new File(Classloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())) {
            return jar.stream().map(ZipEntry::getName)
                    .filter(line -> !line.endsWith(".class"))
                    .filter(line -> line.startsWith(packageName.replace(".", "/")))
                    .filter(line -> Arrays.stream(JarPathBlacklist).noneMatch(line::contains))
                    .map(line -> line.replace("/", "."))
                    .map(line -> line.endsWith(".") ? line.substring(0, line.length() - 1) : line)
                    .filter(line -> !line.equals(packageName))
                    .filter(line -> line.substring(packageName.length() + 1).split("\\.").length == 1)
                    .map(line -> {
                        if (packageName.isEmpty()) {
                            return line;
                        }
                        return line.substring(packageName.length() + 1);
                    })
                    .peek(System.out::println)
                    .collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            System.err.println("Could not load classes from jar file.");
            System.exit(0);
            return Collections.emptySet();
        }
    }

    private Set<Class<? extends T>> loadClassesFromJar() {
        try (JarFile jar =
                     new JarFile(new File(Classloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())) {
            return jar.stream().filter(this::filterValidClassesFromNameJar).map(this::extractClassNameFromJarEntry).filter(line -> !line.contains("/")).map(this::filterClasses).collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Could not load classes from jar file.");
            return Collections.emptySet();
        }
    }

    private boolean filterValidClassesFromNameJar(JarEntry entry) {
        return entry.getName().startsWith(packageName.replace(".", "/") + "/") && entry.getName().endsWith(".class");
    }

    private String extractClassNameFromJarEntry(JarEntry entry) {
        return entry.getName().substring(packageName.length() + 1);
    }

    protected abstract Class<? extends T> filterClasses(String line);

    protected Class<?> defaultClassFilter(String line) {
        try {
            String classPath = packageName + "." + line.substring(0, line.lastIndexOf('.')/*remove ".class" from
            line*/);
            if (classPath.startsWith(".")) {
                classPath = classPath.substring(1);
            }
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            LOGGER.trace(e);
        }
        return null;
    }

    protected Class<? extends T> defaultImplementsFilter(String line, Class<T> interfaceToImplement) {
        Class<?> clazz = defaultClassFilter(line);
        if (interfaceToImplement.isAssignableFrom(clazz) && !clazz.equals(interfaceToImplement)) {
            return clazz.asSubclass(interfaceToImplement);
        }
        return null;
    }

    private boolean isJar() {
        try (JarFile ignored =
                     new JarFile(new File(Classloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())) {
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }
}
