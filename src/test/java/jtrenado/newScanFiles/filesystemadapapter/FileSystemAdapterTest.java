package jtrenado.newScanFiles.filesystemadapapter;


import jtrenado.newScanFiles.adapters.filesystemadapapter.FileSystemAdapter;
import jtrenado.newScanFiles.application.Properties;
import jtrenado.newScanFiles.domain.entities.File;
import jtrenado.scanFiles.infrastructure.entities.FileType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class FileSystemAdapterTest {

    @InjectMocks
    private FileSystemAdapter fileSystemAdapter;

    @Mock
    private Properties properties;

    private List<String> files = List.of("/files/dir2/other", "/files/dir1/dir12/video.avi", "/files/dir1/dir11/image.jpg");


    @Test
    @SneakyThrows
    void exploreAllFiles() {


        Path path = Path.of(ClassLoader.getSystemResource("files").getPath());
        Flux<Path> result = fileSystemAdapter.exploreAllFiles(path);

        List<String> remainingFiles = new ArrayList<>(files);

        StepVerifier.create(result)
                .assertNext(p -> deleteFile(p, remainingFiles))
                .assertNext(p -> deleteFile(p, remainingFiles))
                .assertNext(p -> deleteFile(p, remainingFiles))
                .verifyComplete();

        assertEquals(0, remainingFiles.size());


    }

    @Test
    void readFile() {

        Path path = Path.of(ClassLoader.getSystemResource("files").getPath());
        Path file = Path.of(path.toString(), "dir1", "dir11", "image.jpg");

        when(properties.getVideoExtensions()).thenReturn(List.of("avi"));
        when(properties.getImageExtensions()).thenReturn(List.of("jpg"));

        Optional<File> result = fileSystemAdapter.readFile(file).blockOptional();

        assertTrue(result.isPresent());
        assertEquals(FileType.IMAGE, result.get().getType());


    }

    private void deleteFile(Path p, List<String> remainingFiles) {

        Optional<String> file = remainingFiles.stream().filter(s -> p.toString().endsWith(s)).findFirst();

        assertTrue(file.isPresent());
        remainingFiles.remove(file.get());


    }


}
