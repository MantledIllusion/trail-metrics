package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

public class MetricsTrail {

    private static ThreadLocal<MetricsTrail> THREADLOCAL = new ThreadLocal<>();

    private final UUID trailId;
    private final Set<MetricsTrailConsumer.MetricsTrailConsumerQueue> queues = Collections.synchronizedSet(
            new IdentityHashMap<MetricsTrailConsumer.MetricsTrailConsumerQueue, Void>().keySet());

    private MetricsTrail(UUID trailId) {
        this.trailId = trailId;
    }

    public static boolean hasTrail() {
        return THREADLOCAL.get() != null;
    }

    public static synchronized MetricsTrail beginTrail() {
        return beginTrail(UUID.randomUUID());
    }

    public static synchronized MetricsTrail beginTrail(UUID trailId) {
        if (THREADLOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + trailId + " for thread " + Thread.currentThread() +
                    "; the thread is already identified by trail " + THREADLOCAL.get().trailId);
        }
        THREADLOCAL.set(new MetricsTrail(trailId));
        return THREADLOCAL.get();
    }

    public static synchronized MetricsTrail getTrail() {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot get trail; thread current is not identified by one");
        }
        return THREADLOCAL.get();
    }

    public static synchronized MetricsTrail hook(MetricsTrailConsumer consumer) {
        MetricsTrail trail;
        if (THREADLOCAL.get() == null) {
            trail = new MetricsTrail(UUID.randomUUID());
            THREADLOCAL.set(trail);
        } else {
            trail = THREADLOCAL.get();
        }
        trail.queues.add(consumer.queueFor(trail.trailId));
        return trail;
    }

    public static void commit(Metric metric) {
        MetricsTrail trail = THREADLOCAL.get();
        if (trail != null) {
            trail.queues.parallelStream().forEach(queue -> queue.enqueue(metric));
        }
    }

    public static synchronized void endTrail() {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; thread current is not identified by one");
        }
        MetricsTrail trail = THREADLOCAL.get();
        trail.queues.parallelStream().forEach(queue -> queue.onTrailEnd());
        trail.queues.clear();
    }
}
