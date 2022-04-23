package com.example.demo.application.subscribers;

import com.example.demo.application.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class ChecksumProcessor extends CustomSubscriber {

    @Autowired
    private SubmissionPublisher<Task> processHashPublisher;


    @Autowired
    private SubmissionPublisher<Task> saveTaskPublisher;

    private final int MAX = 1;


    @PostConstruct
    void init() {
        processHashPublisher.subscribe(this);
    }


    @Override
    public void onNext(Task task) {
        if (isCandidate(task)) {
            task.lock(this);
            process(task);
            task.free(this);
            saveTaskPublisher.submit(task);
        } else {
            log.error("¿¿¿¿¿¿");
        }
        requestNext();
    }

    // Candidato para abstract
    private boolean isCandidate(Task task) {
        return task.getHash() == null;
    }

    private void requestNext() {
        onNextAmount++;
        if (onNextAmount % MAX == 0) {
            subscription.request(MAX);
        }
    }


    private void process(Task task) {

        log.info("Processing hash" + task);

        try {
            Thread.sleep(130);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.setHash("hash");
        log.info("Completing hash" + task);


    }
}
