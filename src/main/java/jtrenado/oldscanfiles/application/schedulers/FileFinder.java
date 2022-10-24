package jtrenado.oldscanfiles.application.schedulers;

import jtrenado.oldscanfiles.application.configuration.Properties;
import jtrenado.oldscanfiles.infrastructure.entities.File;
import jtrenado.oldscanfiles.infrastructure.entities.FileType;
import jtrenado.oldscanfiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileFinder {

    @Autowired
    private Properties properties;

    @Autowired
    private FileRepository fileRepository;

    private AtomicInteger numFiles;
    private AtomicInteger numNewFiles;

    @Scheduled(fixedDelayString = "${app.file-finder.fixed-delay}", initialDelayString = "${app.file-finder.initial-delay}")
    public void reloadFiles() {

        log.info("Reload files in paths");

        numFiles = new AtomicInteger();
        numNewFiles = new AtomicInteger();

        List<Path> p = properties.getPaths().stream().map(Paths::get).collect(Collectors.toList());

        boolean allDirectories = p.stream().map(Files::isDirectory).allMatch(Boolean.TRUE::equals);

        if (allDirectories) {
            Flux.fromIterable(p).doOnNext(this::reloadFiles).doOnComplete(() -> log.info("FileFinder completed")).subscribe();
            log.info("{} new files loaded from a total of {}", numNewFiles.get(), numFiles.get());
        } else {
            log.error("Not all paths are directories");
        }

    }

    private void reloadFiles(Path path) {

        try {

            Flux<Path> files = Flux.fromStream(Files.walk(path).parallel()).log();


            files.filter(Files::isRegularFile).doOnNext(f -> numFiles.incrementAndGet()).filter(f -> !ignore(f)).map(Path::toAbsolutePath).filter(p -> {
                boolean exists = fileRepository.existsByPath(p.toString()).toFuture().join();
                if (exists) {
                    log.debug("Already in DB {}", p.toString());
                }
                return !exists;
            }).map(this::create).flatMap(fileRepository::save).doOnNext(f -> {
                numNewFiles.incrementAndGet();
                log.debug("Saving {}", f);
            }).subscribe();

        } catch (IOException e) {
            log.error(path.toString(), e);
        }
    }

    private File create(Path path) {
        FileType fileType = getFileType(path);
        return File.builder().path(path.toString()).type(fileType).build();
    }

    private boolean ignore(Path path) {
        FileType fileType = getFileType(path);
        boolean ignore = properties.isIgnoreImages() && fileType.equals(FileType.IMAGE) || properties.isIgnoreVideos() && fileType.equals(FileType.VIDEO) || properties.isIgnoreOthers() && fileType.equals(FileType.UNKNOWN);
        if (ignore) {
            log.debug("Ignoring " + path.toString());
        }
        return ignore;
    }

    private FileType getFileType(Path path) {
        Path absolutePath = path.toAbsolutePath();
        String extension = getExtensionByStringHandling(absolutePath.toString()).orElse("");
        return getFileType(extension);
    }

    private FileType getFileType(String extension) {
        String ext = extension.toLowerCase(Locale.ROOT);
        if (properties.getVideoExtensions().contains(ext)) {
            return FileType.VIDEO;
        } else if (properties.getImageExtensions().contains(ext)) {
            return FileType.IMAGE;
        } else {
            return FileType.UNKNOWN;
        }
    }

    public Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

}
