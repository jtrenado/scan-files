package jtrenado.scanFiles.application.subscribers;

import jtrenado.scanFiles.application.dto.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;

@Slf4j
public abstract class CustomSubscriber implements Flow.Subscriber<Task> {

    Flow.Subscription subscription;
    int onNextAmount;

    public void request() {
        subscription.request(1);
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
        subscription = s;
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        log.info("{}: done", this.getClass().getCanonicalName());
    }

}
