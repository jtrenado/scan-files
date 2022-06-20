package jtrenado.scanFiles.application.subscribers;

import jtrenado.scanFiles.application.dto.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class FootprintProcessor extends CustomSubscriber {

    @Autowired
    private SubmissionPublisher<Task> processFootprintPublisher;

    @Autowired
    private SubmissionPublisher<Task> saveTaskPublisher;

    private final int MAX = 1;

    @PostConstruct
    void init() {
        processFootprintPublisher.subscribe(this);
    }

    @Override
    public void onNext(Task task) {

        if (isCandidate(task)) {
            task.lock(this);
            process(task);
            task.free(this);
            saveTaskPublisher.submit(task);
        } else {
            log.error("????");
        }
        requestNext();
    }

    // Candidato para abstract
    private boolean isCandidate(Task task) {
        return task.getFootprint() == null;
    }

    private void requestNext() {
        onNextAmount++;
        if (onNextAmount % MAX == 0) {
            subscription.request(MAX);
        }
    }

    private void process(Task task) {

        log.info("Processing footprint" + task);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.setFootprint("footprint");
        log.info("Completing footprint" + task);

    }
}
