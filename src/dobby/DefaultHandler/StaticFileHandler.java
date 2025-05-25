package dobby.DefaultHandler;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.files.service.StaticFileService;
import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;

/**
 * Handler for static files
 */
@RegisterFor(IStaticFileHandler.class)
public class StaticFileHandler implements IStaticFileHandler {
    private final IStaticFileService staticFileService;

    @Inject
    public StaticFileHandler(IStaticFileService staticFileService) {
        this.staticFileService = staticFileService;
    }

    @Override
    public void handle(HttpContext context) {
        String path = context.getRequest().getPath();
        path = substituteIndexFile(path);

        if (path.split("\\.").length == 1) {
            notFound(context);
            return;
        }

        StaticFile file = staticFileService.get(path);

        if (file == null) {
            notFound(context);
            return;
        }

        context.getResponse().sendFile(file);
    }

    private void notFound(HttpContext context) {
        new RouteNotFoundHandler().handle(context);
    }

    /**
     * If the path ends with a slash, add index.html to the end of the path.
     *
     * @param path path to substitute
     * @return substituted path
     */
    private String substituteIndexFile(String path) {
        int lastSlash = path.lastIndexOf("/");

        if (lastSlash == path.length() - 1) {
            path += "index.html";
        } else if (path.substring(lastSlash + 1).split("\\.").length == 1) {
            path += "/index.html";
        }

        return path;
    }
}
