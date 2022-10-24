package jtrenado.scanfiles.domain.ports;

import jtrenado.scanfiles.domain.entities.File;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface DBRepositoryPort {

    Mono<Boolean> existsPath(Path path);

    Mono<File> save(File file);
}
