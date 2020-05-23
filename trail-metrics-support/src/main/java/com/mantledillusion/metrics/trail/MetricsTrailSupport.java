package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

/**
 * Universal support for {@link Thread} based {@link MetricsTrail}s using {@link ThreadLocal}.
 */
public final class MetricsTrailSupport {

    private static final ThreadLocal<MetricsTrail> THREAD_LOCAL = new ThreadLocal<>();
    private static final Map<Integer, Supplier<MetricsTrailListener>> TRAIL_LISTENERS = new HashMap<>();

    private MetricsTrailSupport() {}

    /**
     * Adds a listener to announce the begin and end of a trail to.
     *
     * @param listener The listener to add; might <b>not</b> be null.
     * @param mode The mode in which the listener should be statically referenced; might <b>not</b> be null.
     */
    public static void addListener(MetricsTrailListener listener, MetricsTrailListener.ReferenceMode mode) {
        synchronized (TRAIL_LISTENERS) {
            if (listener == null) {
                throw new IllegalArgumentException("Cannot add a null listener.");
            } else  if (mode == null) {
                throw new IllegalArgumentException("Cannot add a listener with a null mode.");
            }
            Supplier<MetricsTrailListener> listenerSupplier;
            switch (mode) {
                case HARD:
                    listenerSupplier = () -> listener;
                    break;
                case WEAK:
                    WeakReference<MetricsTrailListener> weakReference =  new WeakReference<>(listener);
                    listenerSupplier = weakReference::get;
                    break;
                default:
                    throw new IllegalStateException("Unexpected reference mode: " + mode);
            }
            TRAIL_LISTENERS.put(System.identityHashCode(listener), listenerSupplier);
        }
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener, might be null.
     */
    public static void removeListener(MetricsTrailListener listener) {
        synchronized (TRAIL_LISTENERS) {
            TRAIL_LISTENERS.remove(System.identityHashCode(listener));
        }
    }

    /**
     * Returns whether the calling {@link Thread} is identified by a {@link MetricsTrail}.
     *
     * @return True if the current {@link Thread} is identified by a {@link MetricsTrail}, false otherwise
     */
    public static boolean has() {
        return THREAD_LOCAL.get() != null;
    }

    /**
     * Begins a {@link MetricsTrail} on the current thread using a random {@link UUID}.
     *
     * @return The random UUID of the newly created {@link MetricsTrail}, never null
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
        if (THREAD_LOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + trailId + " for thread " + Thread.currentThread() +
                    "; the current thread is already identified by trail " + THREAD_LOCAL.get().getTrailId());
        } else if (trailId == null) {
            throw new IllegalArgumentException("Cannot begin trail using a null thread id");
        }
        THREAD_LOCAL.set(new MetricsTrail(trailId));
        announce(trailId, true);
    }

    /**
     * Returns the {@link UUID} of the {@link MetricsTrail} that identifies the current {@link Thread}.
     *
     * @return The ID of the current {@link MetricsTrail}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static UUID get() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve the ID of the current trail; current thread is not identified by one");
        }
        return THREAD_LOCAL.get().getTrailId();
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
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREAD_LOCAL.get().hook(consumer);
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
        MetricsTrail trail = THREAD_LOCAL.get();
        if (trail != null) {
            trail.commit(metric);
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
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREAD_LOCAL.get().hasGated();
    }

    /**
     * Returns whether there are {@link Metric}s of the current {@link Thread} {@link MetricsTrail}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} currently being delivered to their consumer by asynchronous tasks.
     *
     * @return True if there is at least one {@link Metric} currently being delivered, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static boolean isDelivering() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail is currently delivering metrics; current thread is not identified by one");
        }
        return THREAD_LOCAL.get().isDelivering();
    }

    /**
     * Ends the {@link MetricsTrail} that identifies the current {@link Thread}.
     *
     * @return The {@link UUID} of the current {@link Thread}'s trail, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static UUID end() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; current thread is not identified by one");
        }
        UUID trailId = THREAD_LOCAL.get().end();
        THREAD_LOCAL.set(null);
        announce(trailId, false);
        return trailId;
    }

    private static void announce(UUID trailId, boolean beginning) {
        synchronized (TRAIL_LISTENERS) {
            Iterator<Supplier<MetricsTrailListener>> iterator = TRAIL_LISTENERS.values().iterator();
            while (iterator.hasNext()) {
                MetricsTrailListener listener = iterator.next().get();
                if (listener == null) {
                    iterator.remove();
                } else {
                    try {
                        listener.announce(trailId, beginning);
                    } catch (Exception e) {
                        System.out.println("Encountered an error while trying to announce " +
                                (beginning ? "beginning" : "ending") + " trail " + trailId + " to listener " +
                                listener + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
