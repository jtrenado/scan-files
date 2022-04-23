package com.example.demo.application;

import com.example.demo.application.subscribers.CustomSubscriber;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ConcurrentModificationException;

@Data
@RequiredArgsConstructor
@ToString(exclude = {"subscriber", "contents"})
@EqualsAndHashCode(of = "path")
@Slf4j
public class Task {


    @NonNull
    private byte[] contents;

    @NonNull
    private Path path;

    private String footprint;

    private String hash;

    private int size;

    private String meta;

    @Setter(AccessLevel.NONE)
    private CustomSubscriber subscriber;

    public synchronized void lock(CustomSubscriber locker) {
        if (this.subscriber == null) {
            this.subscriber = locker;
            log.info("Lock " + this.getPath() + ": " + locker.getClass());
        } else {
            throw new ConcurrentModificationException(locker.getClass() + " is trying to get the task occupied by" + this.subscriber.getClass() + ": " + this);
        }
    }

    public synchronized void free(CustomSubscriber locker) {
        if (this.subscriber == locker) {
            this.subscriber = null;
            log.info("Free " + this.getPath() + ": " + locker.getClass());
        } else {
            throw new ConcurrentModificationException(locker.getClass() + " is trying to free the task occupied by" + this.subscriber.getClass() + ": " + this);
        }
    }

    public boolean isCompleted() {
        return hash != null && footprint != null;
    }
}
