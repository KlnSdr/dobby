package dobby.files.service;

import dobby.files.StaticFile;

public interface IExternalDocRootService {
    StaticFile get(String path);
}
