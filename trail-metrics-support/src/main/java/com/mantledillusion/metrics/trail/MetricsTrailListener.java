package com.mantledillusion.metrics.trail;

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
     * The type of event occurring.
     */
    enum EventType {

        /**
         * A <u>new</u> {@link MetricsTrail} is beginning on the current {@link Thread}.
         */
        BEGIN(true),

        /**
         * A <u>pre-existing</u> {@link MetricsTrail} is being bound to the current {@link Thread}.
         */
        BIND(true),

        /**
         * The {@link MetricsTrail} of the current {@link Thread} is released from the {@link Thread}, <u>but not ended</u>.
         */
        RELEASE(false),

        /**
         * The {@link MetricsTrail} of the current {@link Thread} is has ended.
         */
        END(false);

        private final boolean trailActive;

        EventType(boolean trailActive) {
            this.trailActive = trailActive;
        }

        /**
         * Determines whether or not a {@link MetricsTrail} is active on the current {@link Thread} after the event.
         *
         * @return True if a {@link MetricsTrail} is active, false otherwise.
         */
        public boolean isTrailActive() {
            return this.trailActive;
        }
    }

    /**
     * Called to announce the begin or end of {@link MetricsTrail}s.
     *
     * @param trail The trail; might <b>not</b> be null.
     * @param eventType The type of the event; might <b>not</b> be null.
     * @throws Exception Any exception that might occur
     */
    void announce(MetricsTrail trail, EventType eventType) throws Exception;
}
