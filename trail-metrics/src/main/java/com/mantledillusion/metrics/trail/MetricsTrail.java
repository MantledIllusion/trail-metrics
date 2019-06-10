package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

/**
 * A {@link MetricsTrail} is a stream of {@link Metric}s that occurs during a single process of any kind.
 */
public final class MetricsTrail {

    private final UUID trailId;
    private final Set<MetricsTrailConsumer.MetricsTrailConsumerQueue> queues =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * {@link java.lang.reflect.Constructor}.
     *
     * @param trailId The ID that identifies the trail; might <b>not</b> be null.
     */
    public MetricsTrail(UUID trailId) {
        if (trailId == null) {
            throw new IllegalArgumentException("Cannot begin trail using a null thread id");
        }
        this.trailId = trailId;
    }

    /**
     * Returns the ID that identifies this trail.
     *
     * @return The trail ID, never null
     */
    public UUID getTrailId() {
        return trailId;
    }

    /**
     * Hooks the given {@link MetricsTrailConsumer} this {@link MetricsTrail}.
     * <p>
     * To hook the given {@link MetricsTrailConsumer} to the {@link MetricsTrail}, a new
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} is created that will receive the trail's {@link Metric}s
     * and enqueue them for delivery for the consumer.
     * <p>
     * Delivering the {@link Metric}s will depend on the settings made to the {@link MetricsTrailConsumer}.
     *
     * @param consumer The {@link MetricsTrailConsumer} hook; might <b>not</b> be null.
     * @return A new {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}, never null
     */
    public synchronized MetricsTrailConsumer.MetricsTrailConsumerQueue hook(MetricsTrailConsumer consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("Cannot hook a null consumer to a trail");
        }
        MetricsTrailConsumer.MetricsTrailConsumerQueue queue = consumer.queueFor(this.trailId);
        this.queues.add(queue);
        return queue;
    }

    /**
     * Commits the given {@link Metric} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked this {@link MetricsTrail}.
     *
     * @param metric The metric to commit; might <b>not</b> be null.
     */
    public synchronized void commit(Metric metric) {
        MetricValidator.validate(metric);
        this.queues.parallelStream().forEach(queue -> queue.enqueue(metric));
    }

    /**
     * Returns whether there are {@link Metric}s that are enqueued and waiting for any of this {@link MetricsTrail}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} gates to open so they can be delivered.
     *
     * @return True if there is at least one {@link Metric} currently gated, false otherwise
     */
    public synchronized boolean hasGated() {
        return this.queues.stream().anyMatch(queue -> queue.hasGated());
    }

    /**
     * Returns whether there are {@link Metric}s of this {@link MetricsTrail}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} currently being delivered to their consumer by asynchronous tasks.
     *
     * @return True if there is at least one {@link Metric} currently being delivered, false otherwise
     */
    public synchronized boolean isDelivering() {
        return this.queues.stream().anyMatch(queue -> queue.isDelivering());
    }

    /**
     * Ends the {@link MetricsTrail}.
     *
     * @return The {@link UUID} of the current {@link Thread}'s trail, never null
     */
    public synchronized UUID end() {
        this.queues.forEach(queue -> queue.onTrailEnd());
        this.queues.clear();
        return this.trailId;
    }
}
