package dobby.util;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.IConfig;

@RegisterFor(StaticContentDir.class)
public class StaticContentDir {
    private final IConfig config;

    @Inject
    public StaticContentDir(IConfig config) {
        this.config = config;
    }

    public void appendToContentDir(Class<?> baseClass, String location) {
        final String staticContentDirKey = "dobby.staticContent.directory";
        String currentConfig = config.getString(staticContentDirKey, "");

        if (!currentConfig.isEmpty()) {
            currentConfig += ",";
        }

        final String baseClassName = baseClass.getCanonicalName();

        config.setString(staticContentDirKey, currentConfig + baseClassName + "\\" + location);
    }
}
