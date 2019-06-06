package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;

/**
 * Universal support for {@link Thread} based {@link MetricsTrail}s using {@link ThreadLocal}.
 */
public final class MetricsTrailSupport {

    private static ThreadLocal<MetricsTrail> THREADLOCAL = new ThreadLocal<>();

    private MetricsTrailSupport() {}

    /**
     * Returns whether the calling {@link Thread} is identified by a {@link MetricsTrailSupport}.
     *
     * @return True if the current {@link Thread} is identified by a {@link MetricsTrailSupport}, false otherwise
     */
    public static boolean has() {
        return THREADLOCAL.get() != null;
    }

    /**
     * Begins a {@link MetricsTrailSupport} on the current thread using a random {@link UUID}.
     *
     * @throws IllegalStateException If the current {@link Thread} already is identified by a {@link MetricsTrailSupport}, which can be checked using {@link #has()}.
     */
    public static synchronized UUID begin() throws IllegalStateException {
        UUID trailId = UUID.randomUUID();
        begin(trailId);
        return trailId;
    }

    /**
     * Begins a {@link MetricsTrailSupport} on the current thread using the given {@link UUID}.
     *
     * @param trailId The {@link UUID} to identify the new {@link MetricsTrailSupport} by; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static void begin(UUID trailId) throws IllegalStateException {
        if (THREADLOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + trailId + " for thread " + Thread.currentThread() +
                    "; the current thread is already identified by trail " + THREADLOCAL.get().getTrailId());
        } else if (trailId == null) {
            throw new IllegalArgumentException("Cannot begin trail using a null thread id");
        }
        THREADLOCAL.set(new MetricsTrail(trailId));
    }

    /**
     * Returns the {@link UUID} of the {@link MetricsTrailSupport} that identifies the current {@link Thread}.
     *
     * @return The ID of the current {@link MetricsTrailSupport}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static UUID get() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve the ID of the current trail; current thread is not identified by one");
        }
        return THREADLOCAL.get().getTrailId();
    }

    /**
     * Hooks the given {@link MetricsTrailConsumer} to the current {@link Thread}s {@link MetricsTrailSupport}.
     * <p>
     * To hook the given {@link MetricsTrailConsumer} to the {@link MetricsTrailSupport}, a new
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} is created that will receive the trail's {@link Metric}s
     * and enqueue them for delivery for the consumer.
     * <p>
     * Delivering the {@link Metric}s will depend on the settings made to the {@link MetricsTrailConsumer}.
     *
     * @param consumer The {@link MetricsTrailConsumer} hook; might <b>not</b> be null.
     * @return A new {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static MetricsTrailConsumer.MetricsTrailConsumerQueue hook(MetricsTrailConsumer consumer) throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().hook(consumer);
    }

    /**
     * Commits the given {@link Metric} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrailSupport}.
     *
     * @param metric The metric to commit; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static void commit(Metric metric) throws IllegalStateException {
        commit(metric, true);
    }

    /**
     * Commits the given {@link Metric} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrailSupport}.
     *
     * @param metric The metric to commit; might <b>not</b> be null.
     * @param forced True if committing the given {@link Metric} is inevitable, so if the current {@link Thread} is not identified by a trail, an {@link IllegalStateException} has to be thrown.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport} and the commit is forced.
     */
    public static void commit(Metric metric, boolean forced) throws IllegalStateException {
        MetricsTrail trail = THREADLOCAL.get();
        if (trail != null) {
            trail.commit(metric);
        } else if (forced) {
            throw new IllegalStateException("Cannot commit the given metric to the current trail; current thread is not identified by one");
        }
    }

    /**
     * Returns whether there are {@link Metric}s that are enqueued and waiting for any of the current {@link Thread}
     * {@link MetricsTrailSupport}'s {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} gates to open so they can be delivered.
     *
     * @return True if there is at least one {@link Metric} currently gated, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static boolean hasGated() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().hasGated();
    }

    /**
     * Returns whether there are {@link Metric}s of the current {@link Thread} {@link MetricsTrailSupport}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} currently being delivered to their consumer by asynchronous tasks.
     *
     * @return True if there is at least one {@link Metric} currently being delivered, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static boolean isDelivering() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail is currently delivering metrics; current thread is not identified by one");
        }
        return THREADLOCAL.get().isDelivering();
    }

    /**
     * Ends the {@link MetricsTrailSupport} that identifies the current {@link Thread}.
     *
     * @return The {@link UUID} of the current {@link Thread}'s trail, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrailSupport}.
     */
    public static UUID end() throws IllegalStateException {
        if (THREADLOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; current thread is not identified by one");
        }
        UUID trailId = THREADLOCAL.get().end();
        THREADLOCAL.set(null);
        return trailId;
    }
}
