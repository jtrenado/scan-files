package com.example.demo.application;

import com.example.demo.application.subscribers.ChecksumProcessor;
import com.example.demo.application.subscribers.DistributeProcessor;
import com.example.demo.application.subscribers.FootprintProcessor;
import com.example.demo.application.subscribers.MongoProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

@Component
@Slf4j
public class TaskPublisher {

    private static int MAX = 130;
    private static int INCREMENT = 2;

    @Autowired
    private ChecksumProcessor checksumProcessor;

    @Autowired
    private DistributeProcessor distributeProcessor;

    @Autowired
    private FootprintProcessor footprintProcessor;

    @Autowired
    private MongoProcessor mongoProcessor;

    @Autowired
    private Map<Path, Task> tasks;

    @Autowired
    private SubmissionPublisher<Task> distributePublisher;

    int taskNumber = 0;

    int increment = 0;

    int newPendingTasks;

    @Scheduled(fixedDelay = 20000, initialDelay = 10000)
    public void publish() {

        int pendingTasks = tasks.size();

        int n;
        if (pendingTasks == 0 && newPendingTasks > 0) {
            increment += INCREMENT;
            n = MAX + increment;
        } else {
            increment -= INCREMENT;
            if (increment < 0) {
                increment = 0;
            }
            n = MAX + increment - pendingTasks;


            if (n < 0) {
                n = 0;
            }

        }

        int processed = newPendingTasks - pendingTasks;
        int formerPendingTasks = newPendingTasks;
        newPendingTasks = n + pendingTasks;


        for (int i = 0; i < n; i++) {
            Task task = new Task("aa".getBytes(StandardCharsets.UTF_8), Path.of("" + taskNumber));
            tasks.put(task.getPath(), task);
            taskNumber++;
            distributePublisher.submit(task);
        }


        log.info("\nProcessed: " + processed + "/" + formerPendingTasks + "\nPushing: " + n + "\nTo process: " + newPendingTasks + "\nIncrement: " + increment);

        distributeProcessor.request();
        checksumProcessor.request();
        footprintProcessor.request();
        mongoProcessor.request();

    }


}
