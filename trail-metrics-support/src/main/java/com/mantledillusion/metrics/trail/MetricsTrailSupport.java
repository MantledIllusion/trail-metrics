package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

/**
 * Universal support for {@link Thread} based {@link MetricsTrail}s using {@link ThreadLocal}.
 */
public final class MetricsTrailSupport {

    /**
     * The default key when supplying a {@link MetricsTrail}'s ID in a key/value map.
     */
    public static final String DEFAULT_TRAIL_ID_KEY = "correlation-id";

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
        registerListener(System.identityHashCode(listener), listener, mode);
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener, might be null.
     */
    public static void removeListener(MetricsTrailListener listener) {
        unregisterListener(System.identityHashCode(listener));
    }

    /**
     * Adds the given consumer as a permanent consumer to hook to all trails occurring.
     *
     * @param consumer The consumer to add; might <b>not</b> be null.
     * @param mode The mode in which the listener should be statically referenced; might <b>not</b> be null.
     */
    public static void addPersistentHook(MetricsTrailConsumer consumer, MetricsTrailListener.ReferenceMode mode) {
        if (consumer == null) {
            throw new IllegalArgumentException("Cannot hook a null consumer");
        }
        registerListener(System.identityHashCode(consumer), (trail, eventType) -> {
            if (eventType == MetricsTrailListener.EventType.BEGIN) {
                trail.hook(consumer);
            }
        }, mode);
    }

    /**
     * Removes a previously added consumer.
     *
     * @param consumer The consumer, might be null.
     */
    public static void removePersistentHook(MetricsTrailConsumer consumer) {
        unregisterListener(System.identityHashCode(consumer));
    }

    private static void registerListener(Integer id, MetricsTrailListener listener, MetricsTrailListener.ReferenceMode mode) {
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
            TRAIL_LISTENERS.put(id, listenerSupplier);
        }
    }

    private static void unregisterListener(Integer id) {
        synchronized (TRAIL_LISTENERS) {
            TRAIL_LISTENERS.remove(id);
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
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#BEGIN} event to be dispatched.
     *
     * @return The random UUID of the newly created {@link MetricsTrail}, never null
     * @throws IllegalStateException If the current {@link Thread} already is identified by a {@link MetricsTrail},
     * which can be checked using {@link #has()}.
     */
    public static synchronized UUID begin() throws IllegalStateException {
        UUID correlationId = UUID.randomUUID();
        begin(correlationId);
        return correlationId;
    }

    /**
     * Begins a {@link MetricsTrail} on the current thread using the given {@link UUID}.
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#BEGIN} event to be dispatched.
     *
     * @param correlationId The {@link UUID} to identify the new {@link MetricsTrail} by; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is already identified by a {@link MetricsTrail},
     * which can be checked using {@link #has()}.
     */
    public static void begin(UUID correlationId) throws IllegalStateException {
        if (correlationId == null) {
            throw new IllegalArgumentException("Cannot begin trail using a null thread id");
        }
        bind(new MetricsTrail(correlationId), MetricsTrailListener.EventType.BEGIN);
    }

    /**
     * Binds a pre-existing {@link MetricsTrail} on the current thread.
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#BIND} event to be dispatched.
     *
     * @param metricsTrail The {@link MetricsTrail}; might <b>not</b> be null.
     * @throws IllegalStateException If the current {@link Thread} is already identified by a {@link MetricsTrail},
     * which can be checked using {@link #has()}.
     */
    public static void bind(MetricsTrail metricsTrail) throws IllegalStateException {
        bind(metricsTrail, MetricsTrailListener.EventType.BIND);
    }

    private static void bind(MetricsTrail metricsTrail, MetricsTrailListener.EventType eventType) {
        if (metricsTrail == null) {
            throw new IllegalArgumentException("Cannot hook a null metrics trail.");
        } else if (THREAD_LOCAL.get() != null) {
            throw new IllegalStateException("Cannot begin trail " + metricsTrail.getCorrelationId() + " for thread " +
                    Thread.currentThread() + "; the current thread is already identified by trail " +
                    THREAD_LOCAL.get().getCorrelationId());
        }
        THREAD_LOCAL.set(metricsTrail);
        announce(metricsTrail, eventType);
    }

    /**
     * Returns the {@link UUID} of the {@link MetricsTrail} that identifies the current {@link Thread}.
     *
     * @return The ID of the current {@link MetricsTrail}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static UUID id() throws IllegalStateException {
        return get().getCorrelationId();
    }

    /**
     * Returns the {@link MetricsTrail} that identifies the current {@link Thread}.
     *
     * @return Th current {@link MetricsTrail}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static MetricsTrail get() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve the ID of the current trail; current thread is not identified by one");
        }
        return THREAD_LOCAL.get();
    }

    /**
     * Hooks the given {@link MetricsTrailConsumer} to the current {@link Thread}s {@link MetricsTrail}.
     * <p>
     * To hook the given {@link MetricsTrailConsumer} to the {@link MetricsTrail}, a new
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} is created that will receive the trail's {@link Event}s
     * and enqueue them for delivery for the consumer.
     * <p>
     * Delivering the {@link Event}s will depend on the settings made to the {@link MetricsTrailConsumer}.
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
     * Commits the given {@link Event} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrail}.
     * <p>
     * Convenience method for {@link #commit(Event, TrailBehaviourMode)} with {@link TrailBehaviourMode#LENIENT}.
     *
     * @param event The metric to commit; might <b>not</b> be null.
     */
    public static void commit(Event event) throws IllegalStateException {
        commit(event, TrailBehaviourMode.LENIENT);
    }

    /**
     * Commits the given {@link Event} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrail}.
     *
     * @param event The metric to commit; might <b>not</b> be null.
     * @param forced True if committing the given {@link Event} is inevitable, so if the current {@link Thread} is not identified by a trail, an {@link IllegalStateException} has to be thrown.
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail} and the commit is forced.
     */
    public static void commit(Event event, boolean forced) throws IllegalStateException {
        commit(event, forced ? TrailBehaviourMode.STRICT : TrailBehaviourMode.OPTIONAL);
    }

    /**
     * Commits the given {@link Event} to all {@link MetricsTrailConsumer.MetricsTrailConsumerQueue}s hooked to the current {@link Thread}'s {@link MetricsTrail}.
     *
     * @param event The metric to commit; might <b>not</b> be null.
     * @param mode The {@link TrailBehaviourMode} just for the given {@link Event}; might <b>not</b> be null. In case of {@link TrailBehaviourMode#LENIENT} starts a new {@link MetricsTrail}, it ends immediately after dispatching the event.
     */
    public static void commit(Event event, TrailBehaviourMode mode) throws IllegalStateException {
        if (has()) {
            THREAD_LOCAL.get().commit(event);
        } else if (mode == null || mode == TrailBehaviourMode.LENIENT) {
            begin();
            THREAD_LOCAL.get().commit(event);
            end();
        } else if (mode == TrailBehaviourMode.STRICT) {
            throw new IllegalStateException("Cannot commit the given metric to the current trail; current thread is not identified by one");
        }
    }

    /**
     * Returns whether there are {@link Event}s that are enqueued and waiting for any of the current {@link Thread}
     * {@link MetricsTrail}'s {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} gates to open so they can be delivered.
     *
     * @return True if there is at least one {@link Event} currently gated, false otherwise
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static boolean hasGated() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot retrieve whether the current trail has gated metrics; current thread is not identified by one");
        }
        return THREAD_LOCAL.get().hasGated();
    }

    /**
     * Returns whether there are {@link Event}s of the current {@link Thread} {@link MetricsTrail}'s
     * {@link MetricsTrailConsumer.MetricsTrailConsumerQueue} currently being delivered to their consumer by asynchronous tasks.
     *
     * @return True if there is at least one {@link Event} currently being delivered, false otherwise
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
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#END} event to be dispatched.
     *
     * @return The {@link UUID} of the current {@link Thread}'s trail, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static UUID end() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; current thread is not identified by one");
        }
        MetricsTrail metricsTrail = THREAD_LOCAL.get();
        end(metricsTrail);
        THREAD_LOCAL.set(null);
        return metricsTrail.getCorrelationId();
    }

    /**
     * Ends the {@link MetricsTrail} that identifies the current {@link Thread}.
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#END} event to be dispatched.
     *
     * @param metricsTrail The trail to release; might <b>not</b> be null.
     * @throws IllegalStateException If the given {@link MetricsTrail} is null.
     */
    public static void end(MetricsTrail metricsTrail) throws IllegalStateException {
        if (metricsTrail == null) {
            throw new IllegalArgumentException("Cannot release a null trail");
        }
        announce(metricsTrail, MetricsTrailListener.EventType.END);
        metricsTrail.end();
    }

    /**
     * Releases the {@link MetricsTrail} that identifies the current {@link Thread} without ending it.
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#RELEASE} event to be dispatched.
     *
     * @return The {@link MetricsTrail} of the current {@link Thread}, never null
     * @throws IllegalStateException If the current {@link Thread} is not identified by a {@link MetricsTrail}.
     */
    public static MetricsTrail release() throws IllegalStateException {
        if (THREAD_LOCAL.get() == null) {
            throw new IllegalStateException("Cannot end trail; current thread is not identified by one");
        }
        MetricsTrail metricsTrail = THREAD_LOCAL.get();
        release(metricsTrail);
        THREAD_LOCAL.set(null);
        return metricsTrail;
    }

    /**
     * Releases the {@link MetricsTrail} that identifies the current {@link Thread} without ending it.
     * <p>
     * Will cause a {@link MetricsTrailListener.EventType#RELEASE} event to be dispatched.
     *
     * @param metricsTrail The trail to release; might <b>not</b> be null.
     * @throws IllegalStateException If the given {@link MetricsTrail} is null.
     */
    public static void release(MetricsTrail metricsTrail) throws IllegalStateException {
        if (metricsTrail == null) {
            throw new IllegalArgumentException("Cannot release a null trail");
        }
        announce(metricsTrail, MetricsTrailListener.EventType.RELEASE);
    }

    private static void announce(MetricsTrail metricsTrail, MetricsTrailListener.EventType eventType) {
        synchronized (TRAIL_LISTENERS) {
            Iterator<Supplier<MetricsTrailListener>> iterator = TRAIL_LISTENERS.values().iterator();
            while (iterator.hasNext()) {
                MetricsTrailListener listener = iterator.next().get();
                if (listener == null) {
                    iterator.remove();
                } else {
                    try {
                        listener.announce(metricsTrail, eventType);
                    } catch (Exception e) {
                        System.out.println("Encountered an error while trying to announce a " + eventType.name() +
                                " trail " + metricsTrail.getCorrelationId() + " to listener " + listener + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
