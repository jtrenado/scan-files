package jtrenado.scanFiles.application.subscribers;

import jtrenado.scanFiles.application.dto.Task;
import jtrenado.scanFiles.application.services.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class DistributeProcessor extends CustomSubscriber {

    @Autowired
    private SubmissionPublisher<Task> distributePublisher;

    @Autowired
    private SubmissionPublisher<Task> processHashPublisher;

    @Autowired
    private SubmissionPublisher<Task> processFootprintPublisher;

    @Autowired
    private Map<Path, Task> tasks;

    @Autowired
    private StatsService statsService;

    private static final int MAX = 3;

    int onNextAmount;

    @PostConstruct
    void init() {
        distributePublisher.subscribe(this);
    }

    @Override
    public void onNext(Task task) {

        if (task.getHash() == null) {
            processHashPublisher.submit(task);
            //        } else if (task.getFootprint() == null) {
            //            processFootprintPublisher.submit(task);
        } else {
            tasks.remove(task.getPath());
            statsService.incCompletedTasks(1);
            log.debug("Task has been completed: " + task);
        }

        requestNext();
    }

    private void requestNext() {
        onNextAmount++;
        if (onNextAmount % MAX == 0) {
            subscription.request(MAX);
        }
    }

}
