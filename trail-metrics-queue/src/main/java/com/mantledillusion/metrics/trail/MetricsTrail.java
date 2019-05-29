package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

public class MetricsTrail {

    private static ThreadLocal<Set<MetricsTrailConsumer.MetricsTrailConsumerQueue>> THREADLOCAL = new ThreadLocal<>();

    public static synchronized void hook(UUID trailId, MetricsTrailConsumer consumer) {
        if (THREADLOCAL.get() == null) {
            THREADLOCAL.set(Collections.synchronizedSet(
                    new IdentityHashMap<MetricsTrailConsumer.MetricsTrailConsumerQueue, Void>().keySet()));
        }
        THREADLOCAL.get().add(consumer.queueFor(trailId));
    }

    public static void commit(Metric metric) {
        Set<MetricsTrailConsumer.MetricsTrailConsumerQueue> queues = THREADLOCAL.get();
        if (queues != null) {
            queues.parallelStream().forEach(queue -> queue.enqueue(metric));
        }
    }
}
