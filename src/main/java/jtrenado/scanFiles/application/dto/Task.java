package jtrenado.scanFiles.application.dto;

import jtrenado.scanFiles.application.subscribers.CustomSubscriber;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ConcurrentModificationException;

@Data
@Builder
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

    private Integer size;

    private String meta;

    @Setter(AccessLevel.NONE)
    private CustomSubscriber subscriber;

    public synchronized void lock(CustomSubscriber locker) {
        if (this.subscriber == null) {
            this.subscriber = locker;
            log.debug("Lock " + this.getPath() + ": " + locker.getClass());
        } else {
            throw new ConcurrentModificationException(locker.getClass() + " is trying to get the task occupied by" + this.subscriber.getClass() + ": " + this);
        }
    }

    public synchronized void free(CustomSubscriber locker) {
        if (this.subscriber == locker) {
            this.subscriber = null;
            log.debug("Free " + this.getPath() + ": " + locker.getClass());
        } else {
            throw new ConcurrentModificationException(locker.getClass() + " is trying to free the task occupied by" + this.subscriber.getClass() + ": " + this);
        }
    }

    public boolean isCompleted() {
        return hash != null && size != null;
    }
}
