package org.musigma.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;

/**
 *
 */
public class InputStreamPublisherTest extends PublisherVerification<Byte> {

    public InputStreamPublisherTest() {
        super(new TestEnvironment());
    }

    public Publisher<Byte> createPublisher(final long l) {
        return new InputStreamPublisher(() -> new RandomInputStream(l));
    }

    public Publisher<Byte> createFailedPublisher() {
        return new InputStreamPublisher(() -> null);
    }

    private static class RandomInputStream extends InputStream {

        private final Random random = new Random();
        private final long size;
        private final AtomicLong position = new AtomicLong();

        private RandomInputStream(final long size) {
            this.size = size;
        }

        @Override
        public int read() throws IOException {
            return position.getAndIncrement() < size ? random.nextInt(255) : -1;
        }

        @Override
        public void close() throws IOException {
            position.set(size);
        }
    }
}