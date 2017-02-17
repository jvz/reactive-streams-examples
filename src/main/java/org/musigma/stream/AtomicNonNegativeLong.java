package org.musigma.stream;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A restricted atomic long value contained within the inclusive range 0 to {@value Long#MAX_VALUE}.
 */
public class AtomicNonNegativeLong {
    private final AtomicLong value = new AtomicLong();

    public long get() {
        return value.get();
    }

    public long getAndAdd(long delta) {
        long ret, updated;
        do {
            ret = value.get();
            if (ret == Long.MAX_VALUE) {
                return ret;
            }
            updated = ret + delta;
            if (updated < 0) {
                updated = Long.MAX_VALUE;
            }
        } while (!value.compareAndSet(ret, updated));
        return ret;
    }

    public long addAndGet(long delta) {
        long ret, updated;
        do {
            ret = value.get();
            if (ret == Long.MAX_VALUE) {
                return ret;
            }
            updated = ret + delta;
            if (updated < 0) {
                updated = Long.MAX_VALUE;
            }
        } while (!value.compareAndSet(ret, updated));
        return updated;
    }
}
