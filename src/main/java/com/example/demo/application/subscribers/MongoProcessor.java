package com.example.demo.application.subscribers;

import com.example.demo.application.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class MongoProcessor extends CustomSubscriber {


    @Autowired
    private SubmissionPublisher<Task> distributePublisher;


    @Autowired
    private SubmissionPublisher<Task> saveTaskPublisher;

    private final int MAX = 1;


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
        log.info("Save " + task);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
