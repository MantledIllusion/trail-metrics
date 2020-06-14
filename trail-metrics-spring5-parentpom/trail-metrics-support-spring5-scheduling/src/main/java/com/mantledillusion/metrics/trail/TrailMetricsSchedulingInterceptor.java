package com.mantledillusion.metrics.trail;

import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * AspectJ @{@link Aspect} that intercepts @{@link Scheduled} tasks by beginning a {@link MetricsTrail} right before
 * the execution and ending it at a configurable moment.
 * <p>
 * Requires Spring AOP's @{@link EnableAspectJAutoProxy} to be active.
 * <p>
 * Use {@link #TrailMetricsSchedulingInterceptor(SchedulingTrailEndMode)}} or the {@value #PRTY_TRAIL_END_MODE}
 * property to set the header name to use, which is {@value #DEFAULT_TRAIL_END_MODE} by default.
 */
@Aspect
public class TrailMetricsSchedulingInterceptor {

    private static final String ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER = "@annotation(org.springframework.scheduling.annotation.Scheduled)";

    public static final String PRTY_TRAIL_END_MODE = "trailMetrics.scheduling.endMode";
    public static final String DEFAULT_TRAIL_END_MODE = "IMMEDIATE";

    private SchedulingTrailEndMode mode;

    /**
     * Default constructor.
     * <p>
     * Uses {@link SchedulingTrailEndMode#IMMEDIATE}
     */
    public TrailMetricsSchedulingInterceptor() {
        this(SchedulingTrailEndMode.IMMEDIATE);
    }

    /**
     * Advanced constructor.
     *
     * @param mode The mode to end trails with; may <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsSchedulingInterceptor(@Value("${"+PRTY_TRAIL_END_MODE+":"+DEFAULT_TRAIL_END_MODE+"}") String mode) {
        this(SchedulingTrailEndMode.valueOf(mode));
    }

    /**
     * Advanced constructor.
     *
     * @param mode The mode to end trails with; may <b>not</b> be null.
     */
    public TrailMetricsSchedulingInterceptor(SchedulingTrailEndMode mode) {
        setMode(mode);
    }

    /**
     * Returns the used mode when to end task trails.
     *
     * @return The mode, never null
     */
    public SchedulingTrailEndMode getMode() {
        return mode;
    }

    /**
     * Sets the mode to end task trails.
     *
     * @param mode The mode; might <b>not</b> be null.
     */
    public void setMode(SchedulingTrailEndMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Cannot operate on a null trail end mode");
        }
        this.mode = mode;
    }

    @Before(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void before() {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
        MetricsTrailSupport.begin();
    }

    @AfterReturning(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void afterReturning() {
        if ((this.mode == SchedulingTrailEndMode.IMMEDIATE || this.mode == SchedulingTrailEndMode.IMMEDIATE_ON_SUCCESS)
                && MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }

    @AfterThrowing(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void afterThrowing() {
        if ((this.mode == SchedulingTrailEndMode.IMMEDIATE || this.mode == SchedulingTrailEndMode.IMMEDIATE_ON_FAILURE)
                && MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }
}
