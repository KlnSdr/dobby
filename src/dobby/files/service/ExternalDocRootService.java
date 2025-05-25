package dobby.files.service;

import common.inject.annotations.RegisterFor;
import dobby.files.StaticFile;
import dobby.Config;

import java.io.*;

import static dobby.files.service.StaticFileService.determineContentType;

@RegisterFor(IExternalDocRootService.class)
public class ExternalDocRootService implements IExternalDocRootService {
    private static String docRootPath = null;

    public ExternalDocRootService() {
        docRootPath = Config.getInstance().getString("dobby.staticContent.externalDocRoot", null);
    }

    public StaticFile get(String path) {
        if (docRootPath == null) {
            return null;
        }

        final byte[] content;

        try {
            final File file = new File(docRootPath + path);
            FileInputStream fileInputStream = new FileInputStream(file);

            // Read all bytes from the file into a byte array
            content = fileInputStream.readAllBytes();

            // Close the resource
            fileInputStream.close();

        } catch (IOException e) {
            return null;
        }

        final StaticFile staticFile = new StaticFile();
        staticFile.setContentType(determineContentType(path));
        staticFile.setContent(content);
        return staticFile;
    }
}
