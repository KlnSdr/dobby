package dobby.util;

import dobby.Config;

public class StaticContentDir {
    public static void appendToContentDir(Class<?> baseClass, String location) {
        final Config config = Config.getInstance();
        final String staticContentDirKey = "dobby.staticContent.directory";
        String currentConfig = config.getString(staticContentDirKey, "");

        if (!currentConfig.isEmpty()) {
            currentConfig += ",";
        }

        final String baseClassName = baseClass.getCanonicalName();

        config.setString(staticContentDirKey, currentConfig + baseClassName + "\\" + location);
    }
}
