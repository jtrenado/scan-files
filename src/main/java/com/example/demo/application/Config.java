package com.example.demo.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SubmissionPublisher;

@Configuration
public class Config {


    @Bean
    public Map<Path, Task> tasks() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public SubmissionPublisher<Task> processHashPublisher() {
        SubmissionPublisher<Task> publisher = new SubmissionPublisher<>();
        return publisher;
    }

    @Bean
    public SubmissionPublisher<Task> processFootprintPublisher() {
        SubmissionPublisher<Task> publisher = new SubmissionPublisher<>();
        return publisher;
    }

    @Bean
    public SubmissionPublisher<Task> distributePublisher() {
        SubmissionPublisher<Task> publisher = new SubmissionPublisher<>();
        return publisher;
    }

    @Bean
    public SubmissionPublisher<Task> saveTaskPublisher() {
        return new SubmissionPublisher<>();
    }

}
