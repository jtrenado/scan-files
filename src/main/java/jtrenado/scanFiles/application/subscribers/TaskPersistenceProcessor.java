package jtrenado.scanFiles.application.subscribers;

import jtrenado.scanFiles.application.dto.Task;
import jtrenado.scanFiles.application.services.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class TaskPersistenceProcessor extends CustomSubscriber {

    @Autowired
    private SubmissionPublisher<Task> distributePublisher;

    @Autowired
    private SubmissionPublisher<Task> saveTaskPublisher;

    @Autowired
    private FileService fileService;

    private final int MAX = 2;

    @PostConstruct
    void init() {
        saveTaskPublisher.subscribe(this);
    }

    @Override
    public void onNext(Task task) {
        task.lock(this);
        process(task);
        task.free(this);
        distributePublisher.submit(task);
        requestNext();
    }

    private void requestNext() {
        onNextAmount++;
        if (onNextAmount % MAX == 0) {
            subscription.request(MAX);
        }
    }

    private void process(Task task) {
        log.debug("Save " + task);
        fileService.save(task.getPath(), task.getHash(), task.getFootprint(), task.getSize());
    }

}
