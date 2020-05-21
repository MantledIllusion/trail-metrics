package com.mantledillusion.metrics.trail;

import java.util.UUID;

/**
 * A listener for the begin and end of {@link MetricsTrail}s.
 */
public interface MetricsTrailListener {

    /**
     * The mode which is used to reference the listener, since it has to be referenced statically.
     */
    enum ReferenceMode {

        /**
         * Never let the garbage collector collect the listener.
         */
        HARD,

        /**
         * Let the garbage collector collect the listener if it is not referenced somewhere else any more.
         */
        WEAK
    }

    /**
     * Called to announce the begin or end of {@link MetricsTrail}s.
     *
     * @param trailId The ID of the trail; might <b>not</b> be null.
     * @param beginning True if the trail is beginning, false otherwise.
     * @throws Exception Any exception that might occur
     */
    void announce(UUID trailId, boolean beginning) throws Exception;
}
