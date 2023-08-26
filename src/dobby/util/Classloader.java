package dobby.util;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public abstract class Classloader<T> {
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
        InputStream istream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replace(".", "/"));
        if (istream == null) {
            throw new RuntimeException("Could not load views. Package " + packageName + " not found.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        return reader.lines().filter(line -> !line.endsWith(".class")).collect(Collectors.toSet());
    }

    private Set<Class<? extends T>> loadClassesFromDirectory() {
        InputStream istream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replace(".", "/"));
        if (istream == null) {
            throw new RuntimeException("Could not load views. Package " + packageName + " not found.");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));

        return reader.lines().filter(line -> line.endsWith(".class")).map(this::filterClasses).collect(Collectors.toSet());
    }

    private Set<Class<? extends T>> loadClassesFromJar() {
        try (JarFile jar = new JarFile(new File(Classloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())) {
            return jar.stream().filter(this::filterValidClassesFromNameJar).map(this::extractClassNameFromJarEntry).filter(line -> !line.contains("/")).map(this::filterClasses).collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            System.err.println("Could not load classes from jar file.");
            System.exit(0);
            return Collections.emptySet();
        }
    }

    private boolean filterValidClassesFromNameJar(JarEntry entry) {
        return entry.getName().startsWith(packageName.replace(".", "/")) && entry.getName().endsWith(".class");
    }

    private String extractClassNameFromJarEntry(JarEntry entry) {
        return entry.getName().substring(packageName.length() + 1);
    }

    protected abstract Class<? extends T> filterClasses(String line);

    protected Class<?> defaultClassFilter(String line) {
        try {
            String classPath = packageName + "." + line.substring(0, line.lastIndexOf('.')/*remove ".class" from line*/);
            if (classPath.startsWith(".")) {
                classPath = classPath.substring(1);
            }
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Class<? extends T> defaultImplementsFilter(String line, Class<T> interfaceToImplement) {
        Class<?> clazz = defaultClassFilter(line);
        if (interfaceToImplement.isAssignableFrom(clazz)) {
            return clazz.asSubclass(interfaceToImplement);
        }
        return null;
    }

    private boolean isJar() {
        try (JarFile ignored = new JarFile(new File(Classloader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())) {
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }
}
