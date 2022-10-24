package jtrenado.scanfiles.adapters.filesystemadapapter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.LongStream;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@Slf4j
public class FileSystemPublisher implements Publisher<Path> {


    final ExecutorService executor = Executors.newFixedThreadPool(4);
    private List<MySubscription> subscriptions = Collections.synchronizedList(new ArrayList<MySubscription>());

    private final CompletableFuture<Void> terminated = new CompletableFuture<>();

    private Queue<Path> pathsToProcess = new LinkedList<>();
    private Queue<Path> pathsToPush = new LinkedList<>();


    public FileSystemPublisher(Path path) {
        pathsToProcess.add(path);
    }


    @Override
    public void subscribe(Subscriber<? super Path> subscriber) {
        MySubscription subscription = new MySubscription(subscriber, executor);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void waitUntilTerminated() throws InterruptedException {
        try {
            terminated.get();
        } catch (ExecutionException e) {
            log.error("Error terminating", e);
        }
    }

    private class MySubscription implements Subscription {

        private final ExecutorService executor;

        private Subscriber<? super Path> subscriber;
        private AtomicBoolean isCanceled;

        public MySubscription(Subscriber<? super Path> subscriber, ExecutorService executor) {
            this.subscriber = subscriber;
            this.executor = executor;

            isCanceled = new AtomicBoolean(false);
        }

        @Override
        public void request(long n) {
            if (isCanceled.get())
                return;

            if (n < 0)
                executor.execute(() -> subscriber.onError(new IllegalArgumentException()));
            else
                publishItems(n);
        }

        @Override
        public void cancel() {
            isCanceled.set(true);

            synchronized (subscriptions) {
                subscriptions.remove(this);
                if (subscriptions.isEmpty())
                    shutdown();
            }
        }

        private void publishItems(long n) {

            if (log.isDebugEnabled()) {
                log.debug("Request {}", n);
            }

            while (pathsToPush.size() < n && !pathsToProcess.isEmpty()) {
                getMoreFiles();
            }

            if (pathsToPush.size() < n) {
                if (log.isDebugEnabled()) {
                    log.debug("   serving {}", pathsToPush.size());
                }
                pathsToPush.stream().forEach(p -> subscriber.onNext(p));
                shutdown();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("   serving all", n);
                }
                LongStream.range(0, n).forEach(l -> subscriber.onNext(pathsToPush.remove()));
            }


        }

        private synchronized void getMoreFiles() {

            int[] newFiles = {0};
            int[] newDirectories = {0};

            Path pathToProcess = pathsToProcess.remove();
            try (var files = Files.list(pathToProcess)) {
                files.forEach(p ->
                {
                    if (Files.isRegularFile(p)) {
                        pathsToPush.add(p);
                        newFiles[0]++;
                    } else if (Files.isDirectory(p)) {
                        pathsToProcess.add(p);
                        newDirectories[0]++;
                    }

                });

                if (log.isDebugEnabled()) {
                    log.debug("   1 dir -> {} new directories and {} new files", newDirectories[0], newFiles[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void shutdown() {
            log.debug("Shut down executor...");
            executor.shutdown();
            newSingleThreadExecutor().submit(() -> {

                log.debug("Shutdown complete.");
                terminated.complete(null);
            });

            subscriber.onComplete();
        }

    }


}
