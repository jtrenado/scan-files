package jtrenado.newScanFiles.integration;

import jtrenado.newScanFiles.adapters.mongoadapter.entities.FileEntity;
import jtrenado.newScanFiles.adapters.mongoadapter.entities.FileType;
import jtrenado.newScanFiles.adapters.mongoadapter.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"app.file-finder.fixed-delay=1000000000", "app.file-finder.initial-delay=5000"})
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ExploreFilesIT {

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    @Autowired
    private FileRepository fileRepository;


    private static String path;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        path = Path.of(ClassLoader.getSystemResource("files").getPath()).toAbsolutePath().toString();
        registry.add("app.paths", () -> path);

        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void beforeEach() {
        fileRepository.deleteAll().block();
    }

    @Test
    void testInsertNew() {

        List<String> paths = List.of(
                path + "/dir1/dir12/video.avi",
                path + "/dir2/other",
                path + "/dir1/dir11/image.jpg"
        );


        waitUntilFinished();


        Consumer<? super FileEntity> assertion = f -> {
            assertTrue(paths.contains(f.getPath()), "Missing " + f.getPath());
            assertFalse(f.getMissing());
            assertFalse(f.getToDelete());
            assertFalse(f.getArchived());
            assertNull(f.getHash());
            assertNull(f.getFootprint());
            assertNull(f.getPathToOriginal());
            assertNull(f.getDateTime());
            if (f.getPath().endsWith("avi")) {
                assertEquals(FileType.VIDEO, f.getType());
                assertEquals(11l, f.getSize());
            } else if (f.getPath().endsWith("jpg")) {
                assertEquals(FileType.IMAGE, f.getType());
                assertEquals(6l, f.getSize());
            } else {
                assertEquals(FileType.UNKNOWN, f.getType());
                assertEquals(0l, f.getSize());
            }
        };

        StepVerifier.create(fileRepository.findAll())
                .assertNext(assertion)
                .assertNext(assertion)
                .assertNext(assertion)
                .verifyComplete();


    }

    @Test
    void testDoNotOverride() {

        FileEntity file = new FileEntity();
        file.setHash("hash");
        file.setDateTime(LocalDateTime.now());
        file.setPath(path + "/dir2/other");
        file.setType(FileType.IMAGE);
        FileEntity fileBefore = fileRepository.save(file).block();

        waitUntilFinished();

        FileEntity fileAfter = fileRepository.findById(file.getId()).block();

        fileBefore.setDateTime(fileBefore.getDateTime().truncatedTo(ChronoUnit.MILLIS));
        fileAfter.setDateTime(fileAfter.getDateTime().truncatedTo(ChronoUnit.MILLIS));

        // Nothing changed
        assertEquals(fileBefore, fileAfter);


    }


    private void waitUntilFinished() {
        await()
                .pollDelay(Duration.ofMillis(500))
                .pollDelay(Duration.ofMillis(200))
                .atMost(Duration.ofSeconds(10))
                .until(() -> fileRepository.count().block() == 3l);
    }
}
