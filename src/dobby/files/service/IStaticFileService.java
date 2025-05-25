package dobby.files.service;

import dobby.files.StaticFile;

public interface IStaticFileService {
    void storeFile(String path, StaticFile file);
    void deleteFile(String path);
    void storeFileNoEvent(String path, StaticFile file);
    StaticFile get(String path);
}
