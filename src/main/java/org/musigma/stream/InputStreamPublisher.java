package org.musigma.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Example implementation of a byte publisher using an InputStream supplier. Each subscriber will obtain bytes from
 * a new instance of an InputStream. For notes about rules referenced, see the
 * <a href="https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">list of rules</a>.
 */
public class InputStreamPublisher implements Publisher<Byte> {

    private final Supplier<InputStream> streamSupplier;

    public InputStreamPublisher(final Supplier<InputStream> streamSupplier) {
        this.streamSupplier = Objects.requireNonNull(streamSupplier);
    }

    @Override
    public void subscribe(final Subscriber<? super Byte> subscriber) {
        Objects.requireNonNull(subscriber);
        InputStream is = streamSupplier.get();
        InputStreamSubscription s = new InputStreamSubscription(is, subscriber);
        subscriber.onSubscribe(s);
        if (is == null) {
            s.error(new NullPointerException("InputStream returned from Supplier was null"));
        }
    }

    private static class InputStreamSubscription implements Subscription {

        private final InputStream stream;
        private final Subscriber<? super Byte> subscriber;

        private volatile boolean cancelled;
        private final AtomicNonNegativeLong requested = new AtomicNonNegativeLong();

        private InputStreamSubscription(final InputStream stream, final Subscriber<? super Byte> subscriber) {
            this.stream = stream;
            this.subscriber = subscriber;
        }

        @Override
        public void request(final long n) {
            if (cancelled) return;
            if (n <= 0) {
                error(new IllegalArgumentException("Rule 3.9: non-cancelled subscription request must be a positive number"));
                return;
            }
            // to handle rule 3.3 and avoid infinite recursion when Subscriber.onNext() may call Subscription.request(),
            // we will return early if we're already requesting data. this technique was adopted from FluxIterable in Reactor 3.0.x
            if (requested.getAndAdd(n) == 0) {
                if (n == Long.MAX_VALUE) {
                    requestUnbounded();
                } else {
                    requestBounded(n);
                }
            }
        }

        private void requestBounded(final long n) {
            long i = 0, req = n;
            while (!cancelled) {
                while (i < req) {
                    if (cancelled) return;
                    requestNextByte();
                    i++;
                }
                req = requested.get();
                if (i == req) {
                    req = requested.addAndGet(-i);
                    if (req == 0) return;
                    i = 0;
                }
            }
        }

        private void requestUnbounded() {
            while (!cancelled) {
                requestNextByte();
            }
        }

        private void requestNextByte() {
            int read;
            try {
                read = stream.read();
            } catch (IOException e) {
                error(e);
                return;
            }
            if (cancelled) return;
            if (read == -1) {
                subscriber.onComplete();
                cancel();
                return;
            }
            subscriber.onNext((byte) read);
        }

        @Override
        public void cancel() {
            if (cancelled) return;
            try {
                stream.close();
            } catch (IOException e) {
                subscriber.onError(e);
            } finally {
                cancelled = true;
            }
        }

        private void error(final Throwable t) {
            if (cancelled) return;
            subscriber.onError(t);
            cancelled = true;
        }
    }
}
