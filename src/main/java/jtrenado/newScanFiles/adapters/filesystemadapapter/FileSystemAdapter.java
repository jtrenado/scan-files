package jtrenado.newScanFiles.adapters.filesystemadapapter;

import jtrenado.newScanFiles.application.Properties;
import jtrenado.newScanFiles.domain.entities.File;
import jtrenado.newScanFiles.domain.ports.FileSystemPort;
import jtrenado.scanFiles.infrastructure.entities.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileSystemAdapter implements FileSystemPort {

    private final Properties properties;

    @Override
    public Flux<Path> exploreAllFiles(Path path) {
        return Flux.from(new FileSystemPublisher(path));
    }

    @Override
    public Mono<File> readFile(Path path) {
        FileType fileType = getFileType(path);

        try {
            long size = FileChannel.open(path).size();

            var file = File.builder()
                    .path(path.toString())
                    .type(fileType)
                    .archived(false)
                    .missing(false)
                    .toDelete(false)
                    .size(size)
                    .build();
            return Mono.just(file);
        } catch (IOException e) {
            log.error("Cannot open", e);
            return Mono.empty();
        }
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

    private Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

}
