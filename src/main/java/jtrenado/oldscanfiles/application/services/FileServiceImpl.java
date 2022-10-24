package jtrenado.oldscanfiles.application.services;

import jtrenado.oldscanfiles.infrastructure.entities.File;
import jtrenado.oldscanfiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    public Flux<File> getProcessableFiles() {
        return fileRepository.findAllByHashIsNullOrSizeIsNull().flatMap(this::load).filter(f -> !f.isMissing());
    }

    private Mono<File> load(File file) {
        if (!exists(file)) {
            return markAsMissing(file);
        } else {
            return markAsExisting(file);
        }
    }

    @Override
    public Mono<File> markAsMissing(File file) {
        return markAsMissing(file, true);
    }

    @Override
    public Mono<File> markAsExisting(File file) {
        return markAsMissing(file, false);
    }

    private Mono<File> markAsMissing(File file, boolean missing) {
        return fileRepository.findById(file.getId()).doOnNext(f -> f.setMissing(missing)).flatMap(f -> fileRepository.save(f));
    }

    @Override
    public boolean exists(File file) {
        Path p = Paths.get(file.getPath());
        return Files.isRegularFile(p);
    }

    @Override
    public byte[] getContents(File file) throws IOException {
        Path p = Paths.get(file.getPath());
        return Files.readAllBytes(p);
    }

    @Override
    public void save(Path path, String hash, String footprint, int size) {
        Optional<File> file = fileRepository.findByPath(path.toString()).blockOptional();
        if (file.isPresent()) {
            File f = file.get();
            f.setHash(hash);
            f.setFootprint(footprint);
            f.setSize(size);
            fileRepository.save(f).block();
        } else {
            log.error("File not found in mongo {}", path.toString());
        }

    }

}