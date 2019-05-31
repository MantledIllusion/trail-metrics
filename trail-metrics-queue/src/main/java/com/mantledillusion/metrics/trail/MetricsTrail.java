package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

/**
 * A {@link MetricsTrail} is a stream of {@link Metric}s that occurs during a single process of any kind.
 */
public class MetricsTrail {

    private static ThreadLocal<MetricsTrail> THREADLOCAL = new ThreadLocal<>();

    private final UUID trailId;
    private final Set<MetricsTrailConsumer.MetricsTrailConsumerQueue> queues =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private MetricsTrail(UUID trailId) {
        this.trailId = trailId;
    }

    /**
     * Returns whether the calling {@link Thread} is identified by a {@link MetricsTrail}.
     *
     * @return True if the current {@link Thread} is identified by a {@link MetricsTrail}, false otherwise
     */
    public static boolean has() {
        return THREADLOCAL.get() != null;
    }

    /**
     * Begins a {@link MetricsTrail} on the current thread using a random {@link UUID}.
     *
     * @throws IllegalStateException If the current {@link Thread} already is identified by a {@link MetricsTrail}, which can be checked using {@link #has()}.
     */
    public static synchronized UUID begin() throws IllegalStateException {
        UUID trailId = UUID.randomUUID();
        begin(trailId);
        return trailId;
    }

    /**
     * Begins a {@link MetricsTrail} on the current thread using the given {@link UUID}.
     *
     * @param trailId The {@link UUID} to identify the new {@link MetricsTrail} by; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static void begin(UUID trailId) throws IllegalStateException {
        if (THREADLOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + trailId + " for thread " + Thread.currentThread() +
                    "; the current thread is already identified by trail " + THREADLOCAL.get().trailId);
        } else if (trailId == null) {
            throw new IllegalStateException("Cannot begin trail using a null thread id");
        }
        THREADLOCAL.set(new MetricsTrail(trailId));
    }

    /**
     * Hooks the given {@link MetricsTrailConsumer} to the current {@link Thread}s {@link MetricsTrail}.
     * <p>
     * To hook the given {@link MetricsTrailConsumer} to the {@link MetricsTrail}, a new
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} is created that will receive the trail's {@link Metric}s
     * and enqueue them for delivery for the consumer.
     * <p>
     * Delivering the {@link Metric}s will depend on the settings made to the {@link MetricsTrailConsumer}.
     *
     * @param consumer The {@link MetricsTrailConsumer} hook; might <b>not</b> be null.
     * @return A new {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static MetricsTrailConsumer.MetricsTrailConsumerQueue hook(MetricsTrailConsumer consumer) throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        } else if (consumer == null) {
            throw new IllegalArgumentException("Cannot hook a null consumer to a trail");
        }
        MetricsTrail trail = THREADLOCAL.get();
        MetricsTrailConsumer.MetricsTrailConsumerQueue queue = consumer.queueFor(trail.trailId);
        trail.queues.add(queue);
        return queue;
    }

    /**
     * Commits the given {@link Metric} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrail}.
     *
     * @param metric The metric to commit; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static void commit(Metric metric) throws IllegalStateException {
        commit(metric, true);
    }

    /**
     * Commits the given {@link Metric} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrail}.
     *
     * @param metric The metric to commit; might <b>not</b> be null.
     * @param forced True if committing the given {@link Metric} is inevitable, so if the current {@link Thread} is not identified by a trail, an {@link IllegalStateException} has to be thrown.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail} and the commit is forced.
     */
    public static void commit(Metric metric, boolean forced) throws IllegalStateException {
        MetricsTrail trail = THREADLOCAL.get();
        if (trail != null) {
            if (metric == null) {
                throw new IllegalArgumentException("Cannot commit a null metric to a trail");
            }
            trail.queues.parallelStream().forEach(queue -> queue.enqueue(metric));
        } else if (forced) {
            throw new IllegalStateException("Cannot commit the given metric to the current trail; current thread is not identified by one");
        }
    }

    /**
     * Returns whether there are {@link Metric}s that are enqueued and waiting for any of the current {@link Thread}
     * {@link MetricsTrail}'s {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} gates to open so they can be delivered.
     *
     * @return True if there is at least one {@link Metric} currently gated, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static boolean hasGated() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().queues.stream().anyMatch(queue -> queue.hasGated());
    }

    /**
     * Returns whether there are {@link Metric}s of the current {@link Thread} {@link MetricsTrail}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} currently being delivered to their consumer by asynchronous tasks.
     *
     * @return True if there is at least one {@link Metric} currently being delivered, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static boolean isDelivering() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail is currently delivering metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().queues.stream().anyMatch(queue -> queue.isDelivering());
    }

    /**
     * Ends the {@link MetricsTrail} that identifies the current {@link Thread}.
     *
     * @return The {@link UUID} of the current {@link Thread}'s trail, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static UUID end() throws IllegalStateException {
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
