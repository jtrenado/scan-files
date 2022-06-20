package jtrenado.scanFiles.application.schedulers;

import jtrenado.scanFiles.application.configuration.Properties;
import jtrenado.scanFiles.application.dto.Task;
import jtrenado.scanFiles.application.services.FileService;
import jtrenado.scanFiles.application.services.StatsService;
import jtrenado.scanFiles.application.subscribers.ChecksumProcessor;
import jtrenado.scanFiles.application.subscribers.DistributeProcessor;
import jtrenado.scanFiles.application.subscribers.FootprintProcessor;
import jtrenado.scanFiles.application.subscribers.TaskPersistenceProcessor;
import jtrenado.scanFiles.infrastructure.entities.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class TaskPublisher {

    @Autowired
    private ChecksumProcessor checksumProcessor;

    @Autowired
    private DistributeProcessor distributeProcessor;

    @Autowired
    private FootprintProcessor footprintProcessor;

    @Autowired
    private TaskPersistenceProcessor mongoProcessor;

    @Autowired
    private Map<Path, Task> tasks;

    @Autowired
    private SubmissionPublisher<Task> distributePublisher;

    @Autowired
    private FileService fileService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private Properties properties;

    int increment = 0;

    int newPendingTasks;

    @Scheduled(fixedDelayString = "${app.task-publisher.fixed-delay}", initialDelayString = "${app.task-publisher.initial-delay}")
    public void publish() {

        int initialIncrement = properties.getInitialTasksIncrement();
        int initialMax = properties.getInitialMaxTasks();

        int pendingTasks = tasks.size();

        int n;
        if (pendingTasks == 0 && newPendingTasks > 0) {
            increment += initialIncrement;
            n = initialMax + increment;
        } else {
            increment -= initialIncrement;
            if (increment < 0) {
                increment = 0;
            }
            n = initialMax + increment - pendingTasks;

            if (n < 0) {
                n = 0;
            }

        }

        Map<String, Integer> stats = statsService.getStats();
        int processed = newPendingTasks - pendingTasks;
        int formerPendingTasks = newPendingTasks;

        n = submitTasks(n);

        newPendingTasks = n + pendingTasks;

        log.info("\nProcessed: " + stats.get(StatsService.COMPLETED_TASKS) + "/" + formerPendingTasks + "\nPushing: " + n + "\nTo process: " + newPendingTasks + "\nIncrement: " + increment);

        distributeProcessor.request();
        checksumProcessor.request();
        footprintProcessor.request();
        mongoProcessor.request();

    }

    private int submitTasks(int n) {

        List<File> files = fileService.getNextFiles(n);

        AtomicInteger taskNumber = new AtomicInteger();

        files.stream().map(this::createTask).filter(Objects::nonNull).forEach(task -> {
            tasks.put(task.getPath(), task);
            taskNumber.getAndIncrement();
            distributePublisher.submit(task);

        });

        return taskNumber.get();
    }

    private Task createTask(File file) {

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
