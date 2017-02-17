package org.musigma.stream;

import java.io.IOException;
import java.io.OutputStream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.TestEnvironment;

/**
 *
 */
public class OutputStreamSubscriberTest extends SubscriberWhiteboxVerification<Byte> {

    public OutputStreamSubscriberTest() {
        super(new TestEnvironment());
    }

    @Override
    public Subscriber<Byte> createSubscriber(final WhiteboxSubscriberProbe<Byte> probe) {
        return new OutputStreamSubscriber(new NullOutputStream()) {
            @Override
            public void onSubscribe(final Subscription subscription) {
                super.onSubscribe(subscription);
                probe.registerOnSubscribe(new SubscriberPuppet() {
                    @Override
                    public void triggerRequest(final long elements) {
                        subscription.request(elements);
                    }

                    @Override
                    public void signalCancel() {
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void onNext(final Byte b) {
                super.onNext(b);
                probe.registerOnNext(b);
            }

            @Override
            public void onError(final Throwable throwable) {
                super.onError(throwable);
                probe.registerOnError(throwable);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                probe.registerOnComplete();
            }
        };
    }

    @Override
    public Byte createElement(final int element) {
        return (byte) (Math.abs(element) % 255);
    }

    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(final int b) throws IOException {
        }
    }
}