package jtrenado.oldscanfiles.application.services;

import jtrenado.oldscanfiles.infrastructure.entities.File;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {

    Flux<File> getProcessableFiles();

    Mono<File> markAsMissing(File file);

    Mono<File> markAsExisting(File file);

    boolean exists(File file);

    byte[] getContents(File file) throws IOException;

    void save(Path path, String hash, String footprint, int size);
}
