package jtrenado.newScanFiles.adapters.mongoadapter;

import jtrenado.newScanFiles.adapters.mongoadapter.mappers.FileEntityMapper;
import jtrenado.newScanFiles.adapters.mongoadapter.repository.FileRepository;
import jtrenado.newScanFiles.domain.entities.File;
import jtrenado.newScanFiles.domain.ports.DBRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Path;


@Component
@Slf4j
@RequiredArgsConstructor
public class DBRepositoryAdapter implements DBRepositoryPort {

    private final FileRepository fileRepository;

    private final FileEntityMapper mapper;

    @Override
    public Mono<Boolean> existsPath(Path path) {
        log.debug("Check exist: {}", path);
        return fileRepository.existsByPath(path.toAbsolutePath().toString());
    }

    @Override
    public Mono<File> save(File file) {
        log.debug("Save: {}", file.getPath());
        return fileRepository.save(mapper.toEntity(file))
                .doOnNext(f -> log.error("Saved: {}", f))
                .map(mapper::toDomain);
    }
}
