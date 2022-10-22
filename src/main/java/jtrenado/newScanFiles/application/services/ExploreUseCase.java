package jtrenado.newScanFiles.application.services;

import jtrenado.newScanFiles.application.Properties;
import jtrenado.newScanFiles.domain.ports.DBRepositoryPort;
import jtrenado.newScanFiles.domain.ports.FileSystemPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExploreUseCase {


    private final FileSystemPort fileSystemPort;

    private final DBRepositoryPort dbRepositoryPort;

    private final Properties properties;


    public void explore() {

        log.info("Reload files in paths");

        List<Path> paths = properties.getPaths().stream().map(Paths::get).collect(Collectors.toList());

        boolean allDirectories = paths.stream().map(Files::isDirectory).allMatch(Boolean.TRUE::equals);

        if (allDirectories) {
            log.info("Directories {}", paths);
            Flux.fromIterable(paths) // Path or directory
                    .flatMap(fileSystemPort::exploreAllFiles) // Path of file
                    .filterWhen(p -> dbRepositoryPort.existsPath(p).map(b -> !b))
                    .flatMap(fileSystemPort::readFile)
                    .flatMap(dbRepositoryPort::save)
                    .count()
                    .doOnNext(l -> log.info("{} files", l))
                    .block();
        } else {
            log.error("Not all paths are directories: {}", paths);
        }
    }
}
