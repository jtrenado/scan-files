package jtrenado.newScanFiles.domain.ports;

import jtrenado.newScanFiles.domain.entities.File;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface FileSystemPort {

    /**
     * Get a flux that will pull all regular files from a directory recursivelly
     */
    Flux<Path> exploreAllFiles(Path path);

    Mono<File> readFile(Path path);
}
