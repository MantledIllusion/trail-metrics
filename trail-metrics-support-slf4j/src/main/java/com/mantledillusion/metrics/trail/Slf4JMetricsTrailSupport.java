package com.mantledillusion.metrics.trail;

import org.slf4j.MDC;

/**
 * Support for adding a {@link Thread} {@link MetricsTrail}'s ID to the SLF4J {@link MDC} using the key {@link #TRAIL_ID}.
 * <p>
 * Can be activated using {@link #activatePublishToMdc()} on applications startup.
 */
public class Slf4JMetricsTrailSupport {

    public static final String TRAIL_ID = "trailId";

    private static final MetricsTrailListener METRICS_TRAIL_LISTENER = (trail, eventType) -> {
        if (eventType.isTrailActive()) {
            MDC.put(TRAIL_ID, trail.getTrailId().toString());
        } else {
            MDC.remove(TRAIL_ID);
        }
    };

    private Slf4JMetricsTrailSupport() {}

    /**
     * Will cause every {@link Thread} based {@link MetricsTrail}'s ID to automatically be added to SLF4J's {@link MDC}
     * when the trail is begun and removed once the trail ends.
     */
    public static void activatePublishToMdc() {
        MetricsTrailSupport.addListener(METRICS_TRAIL_LISTENER, MetricsTrailListener.ReferenceMode.WEAK);
    }

    /**
     * Will stop {@link Thread} based {@link MetricsTrail}'s ID to added to SLF4J's {@link MDC}.
     */
    public static void deactivatePublishToMdc() {
        MetricsTrailSupport.removeListener(METRICS_TRAIL_LISTENER);
    }
}
