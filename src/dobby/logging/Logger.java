package dobby.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Logger {
    private static LogLevel logLevel = LogLevel.DEBUG;
    private final Class<?> clazz;

    public Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static void setMaxLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void trace(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();

        String stackTrace = writer.toString();
        log(LogLevel.ERROR, stackTrace);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    private void log(LogLevel level, String message) {
        if (level.ordinal() <= logLevel.ordinal()) {
            System.out.printf("%s [%s] %s %s%n", new Date(), clazz.getCanonicalName(), level.getColorized(), message);
        }
    }
}
