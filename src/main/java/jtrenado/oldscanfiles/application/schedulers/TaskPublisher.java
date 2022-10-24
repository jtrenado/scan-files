package jtrenado.oldscanfiles.application.schedulers;

import jtrenado.oldscanfiles.application.configuration.Properties;
import jtrenado.oldscanfiles.application.consumers.ChecksumConsumer;
import jtrenado.oldscanfiles.application.dto.Task;
import jtrenado.oldscanfiles.application.services.FileService;
import jtrenado.oldscanfiles.infrastructure.entities.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class TaskPublisher {

    @Autowired
    private Map<Path, Task> tasks;

    @Autowired
    private FileService fileService;

    @Autowired
    private Properties properties;

    @Autowired
    private ChecksumConsumer checksumConsumer;

    private AtomicBoolean isExecuting = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.task-publisher.fixed-delay}", initialDelayString = "${app.task-publisher.initial-delay}")
    public void publish() {

        if (isExecuting.compareAndSet(false, true)) {

            log.info("Starting TaskPublisher");
            fileService.getProcessableFiles().parallel(Runtime.getRuntime().availableProcessors()).runOn(Schedulers.boundedElastic()).map(this::createTask).doOnNext(checksumConsumer).doOnNext(task -> fileService.save(task.getPath(), task.getHash(), task.getFootprint(), task.getSize())).doOnComplete(() -> log.info("TaskPublisher completed")).doAfterTerminate(() -> isExecuting.set(false)).subscribe();
            log.info("Ending TaskPublisher");

        } else {
            log.info("Skipping TaskPublisher");
        }
    }

    private Task createTask(File file) {

        log.debug("Create {}", file.getId());

        if (fileService.exists(file)) {

            try {
                byte[] b = fileService.getContents(file);
                return Task.builder().footprint(file.getFootprint()).hash(file.getHash()).size(file.getSize()).path(Paths.get(file.getPath())).contents(b).build();
            } catch (IOException e) {
                log.error("Error reading file: {}", file.getPath(), e);
            }
        } else {
            log.error("File does not exist: {}", file.getPath());
        }

        return null;
    }

}
