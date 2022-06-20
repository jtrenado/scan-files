package jtrenado.scanFiles.application.services;

import jtrenado.scanFiles.infrastructure.entities.File;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {

    List<File> getNextFiles(int n);

    void markAsMissing(File file);

    void markAsExisting(File file);

    boolean exists(File file);

    byte[] getContents(File file) throws IOException;

    void save(Path path, String hash, String footprint, int size);
}
