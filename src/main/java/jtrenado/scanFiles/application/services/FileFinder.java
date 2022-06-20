package jtrenado.scanFiles.application.services;

import jtrenado.scanFiles.application.configuration.Properties;
import jtrenado.scanFiles.infrastructure.entities.File;
import jtrenado.scanFiles.infrastructure.entities.FileType;
import jtrenado.scanFiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public synchronized void reloadFiles() {

        log.info("Reload files in paths");

        numFiles = new AtomicInteger();
        numNewFiles = new AtomicInteger();

        List<Path> p = properties.getPaths().stream().map(Paths::get).collect(Collectors.toList());

        boolean allDirectories = p.stream().map(Files::isDirectory).allMatch(Boolean.TRUE::equals);

        if (allDirectories) {
            p.parallelStream().forEach(this::reloadFiles);
            log.info("{} new files loaded from a total of {}", numNewFiles.get(), numFiles.get());
        } else {
            log.error("Not all paths are directories");
        }

    }

    private void reloadFiles(Path path) {

        try {
            Files.walk(path).parallel().filter(Files::isRegularFile).forEach(this::persist);
        } catch (IOException e) {
            log.error(path.toString(), e);
        }
    }

    private void persist(Path path) {

        Path absolutePath = path.toAbsolutePath();
        Optional<File> file = fileRepository.findByPath(absolutePath.toString());
        if (file.isEmpty()) {
            String extension = getExtensionByStringHandling(absolutePath.toString()).orElse("");
            FileType fileType = getFileType(extension);
            if (!ignore(fileType)) {
                File f = File.builder().path(absolutePath.toString()).type(fileType).build();
                fileRepository.save(f);
                numNewFiles.incrementAndGet();
                log.debug("Saving " + absolutePath.toString());
            } else {
                log.debug("Ignoring " + absolutePath.toString());
            }
        } else {
            log.debug("Already in DB " + absolutePath.toString());
        }
        numFiles.incrementAndGet();

    }

    private boolean ignore(FileType fileType) {
        return properties.isIgnoreImages() && fileType.equals(FileType.IMAGE) || properties.isIgnoreVideos() && fileType.equals(FileType.VIDEO) || properties.isIgnoreOthers() && fileType.equals(FileType.UNKNOWN);
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
