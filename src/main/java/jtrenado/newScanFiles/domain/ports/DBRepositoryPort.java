package jtrenado.newScanFiles.domain.ports;

import jtrenado.newScanFiles.domain.entities.File;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface DBRepositoryPort {

    Mono<Boolean> existsPath(Path path);

    Mono<File> save(File file);
}
