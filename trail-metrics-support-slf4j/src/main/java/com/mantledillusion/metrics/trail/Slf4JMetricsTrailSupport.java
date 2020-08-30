package com.mantledillusion.metrics.trail;

import org.slf4j.MDC;

/**
 * Support for adding a {@link Thread} {@link MetricsTrail}'s ID to the SLF4J {@link MDC} using the key {@link #DEFAULT_KEY}.
 * <p>
 * Can be activated using {@link #activatePublishToMdc()} on applications startup.
 */
public class Slf4JMetricsTrailSupport {

    private static class MDCTrailListener implements MetricsTrailListener {

        private String key = DEFAULT_KEY;

        @Override
        public void announce(MetricsTrail trail, EventType eventType) throws Exception {
            if (eventType.isTrailActive()) {
                MDC.put(key, trail.getCorrelationId().toString());
            } else {
                MDC.remove(key);
            }
        }
    }

    private static final MDCTrailListener METRICS_TRAIL_LISTENER = new MDCTrailListener();

    public static final String DEFAULT_KEY = MetricsTrailSupport.DEFAULT_TRAIL_ID_KEY;

    private Slf4JMetricsTrailSupport() {}

    /**
     * Will cause every {@link Thread} based {@link MetricsTrail}'s ID to automatically be added to SLF4J's {@link MDC}
     * when the trail is begun and removed once the trail ends.
     * <p>
     * Uses the {@value #DEFAULT_KEY} as MDC key.
     */
    public static void activatePublishToMdc() {
        activatePublishToMdc(DEFAULT_KEY);
    }

    /**
     * Will cause every {@link Thread} based {@link MetricsTrail}'s ID to automatically be added to SLF4J's {@link MDC}
     * when the trail is begun and removed once the trail ends.
     *
     * @param mdcKey The key to use in the MDC for the trail ID; might <b>not</b> be null.
     */
    public static void activatePublishToMdc(String mdcKey) {
        if (mdcKey == null) {
            throw new IllegalArgumentException("Cannot put the trail into the MDC using a null key");
        }

        synchronized (METRICS_TRAIL_LISTENER) {
            METRICS_TRAIL_LISTENER.key = mdcKey;
            MetricsTrailSupport.addListener(METRICS_TRAIL_LISTENER, MetricsTrailListener.ReferenceMode.WEAK);
        }
    }

    /**
     * Will stop {@link Thread} based {@link MetricsTrail}'s ID to added to SLF4J's {@link MDC}.
     */
    public static void deactivatePublishToMdc() {
        synchronized (METRICS_TRAIL_LISTENER) {
            MetricsTrailSupport.removeListener(METRICS_TRAIL_LISTENER);
        }
    }
}
