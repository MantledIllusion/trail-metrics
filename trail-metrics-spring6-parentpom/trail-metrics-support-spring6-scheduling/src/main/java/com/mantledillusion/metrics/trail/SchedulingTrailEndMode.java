package com.mantledillusion.metrics.trail;

/**
 * The mode determining when to end {@link MetricsTrail}s after a scheduled task completes.
 */
public enum SchedulingTrailEndMode {

    /**
     * End all {@link MetricsTrail}s immediately when a task finishes.
     */
    IMMEDIATE,

    /**
     * Only end {@link MetricsTrail}s immediately if the task finishes successfully.
     * <p>
     * Trails of failing tasks will end delayed until right before the next task execution begins.
     */
    IMMEDIATE_ON_SUCCESS,

    /**
     * Only end {@link MetricsTrail}s immediately if the task finishes throwing an exception.
     * <p>
     * Trails of successful tasks will end delayed until right before the next task execution begins.
     */
    IMMEDIATE_ON_FAILURE,

    /**
     * Delay ending all {@link MetricsTrail}s until right before the next task execution begins.
     */
    DELAYED
}
