package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

public class MetricsTrail {

    private static ThreadLocal<MetricsTrail> THREADLOCAL = new ThreadLocal<>();

    private final UUID trailId;
    private final Set<MetricsTrailConsumer.MetricsTrailConsumerQueue> queues =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private MetricsTrail(UUID trailId) {
        this.trailId = trailId;
    }

    public static boolean has() {
        return THREADLOCAL.get() != null;
    }

    public static synchronized UUID begin() {
        UUID trailId = UUID.randomUUID();
        begin(trailId);
        return trailId;
    }

    public static void begin(UUID trailId) {
        if (THREADLOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + trailId + " for thread " + Thread.currentThread() +
                    "; the current thread is already identified by trail " + THREADLOCAL.get().trailId);
        }
        THREADLOCAL.set(new MetricsTrail(trailId));
    }

    public static MetricsTrailConsumer.MetricsTrailConsumerQueue hook(MetricsTrailConsumer consumer) {
        MetricsTrail trail;
        if (THREADLOCAL.get() == null) {
            trail = new MetricsTrail(UUID.randomUUID());
            THREADLOCAL.set(trail);
        } else {
            trail = THREADLOCAL.get();
        }
        MetricsTrailConsumer.MetricsTrailConsumerQueue queue = consumer.queueFor(trail.trailId);
        trail.queues.add(queue);
        return queue;
    }

    public static UUID commit(Metric metric) {
        MetricsTrail trail = THREADLOCAL.get();
        if (trail != null) {
            trail.queues.parallelStream().forEach(queue -> queue.enqueue(metric));
            return trail.trailId;
        }
        return null;
    }

    public static boolean hasGated() {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().queues.stream().anyMatch(queue -> queue.hasGated());
    }

    public static boolean isDelivering() {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail is currently delivering metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().queues.stream().anyMatch(queue -> queue.isDelivering());
    }

    public static UUID end() {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; current thread is not identified by one");
        }
        MetricsTrail trail = THREADLOCAL.get();
        trail.queues.parallelStream().forEach(queue -> queue.onTrailEnd());
        trail.queues.clear();
        THREADLOCAL.set(null);
        return trail.trailId;
    }
}
