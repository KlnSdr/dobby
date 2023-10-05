package dobby.DefaultHandler;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;

public class StaticFileHandler implements IRequestHandler {
    @Override
    public void handle(HttpContext context) {
        String path = context.getRequest().getPath();
        path = substituteIndexFile(path);

        if (path.split("\\.").length == 1) {
            notFound(context);
            return;
        }

        StaticFile file = StaticFileService.getInstance().get(path);

        if (file == null) {
            notFound(context);
            return;
        }

        context.getResponse().setHeader("Content-Type", file.getContentType());
        context.getResponse().setBodyBytes(file.getContent());
    }

    private void notFound(HttpContext context) {
        new RouteNotFoundHandler().handle(context);
    }

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
