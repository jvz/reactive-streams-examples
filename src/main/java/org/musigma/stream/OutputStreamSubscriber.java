package org.musigma.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Example implementation of a byte subscriber using an OutputStream.
 */
public class OutputStreamSubscriber implements Subscriber<Byte> {

    private final OutputStream stream;
    private Subscription subscription;
    private volatile boolean cancelled;

    public OutputStreamSubscriber(final OutputStream stream) {
        this.stream = Objects.requireNonNull(stream);
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        Objects.requireNonNull(subscription);
        if (cancelled) return;
        if (this.subscription != null) {
            subscription.cancel();
            return;
        }
        this.subscription = subscription;
    }

    @Override
    public void onNext(final Byte b) {
        try {
            stream.write(Objects.requireNonNull(b));
        } catch (IOException e) {
            e.printStackTrace();
            subscription.cancel();
            cancelled = true;
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        Objects.requireNonNull(throwable);
        throwable.printStackTrace();
        cancelled = true;
    }

    @Override
    public void onComplete() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cancelled = true;
    }
}
