package dobby.files.service;

import dobby.files.StaticFile;
import dobby.util.Config;

import java.io.*;
import java.nio.file.Path;

import static dobby.files.service.StaticFileService.determineContentType;

public class ExternalDocRootService {
    private static ExternalDocRootService instance = null;
    private static String docRootPath = null;

    private ExternalDocRootService() {
        docRootPath = Config.getInstance().getString("dobby.staticContent.externalDocRoot", null);
    }

    public static ExternalDocRootService getInstance() {
        if (instance == null) {
            instance = new ExternalDocRootService();
        }
        return instance;
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
