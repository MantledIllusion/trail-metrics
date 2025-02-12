package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.aspectj.lang.JoinPoint;
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
 * Use {@link #TrailMetricsSchedulingInterceptor(SchedulingTrailEndMode, boolean, boolean)}}} or the
 * {@value #PRTY_TRAIL_END_MODE} property to set the header name to use, which is {@value #DEFAULT_TRAIL_END_MODE} by
 * default.
 */
@Aspect
public class TrailMetricsSchedulingInterceptor {

    private static final ThreadLocal<Long> TASK_DURATION = new ThreadLocal<>();

    private static final String ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER = "@annotation(org.springframework.scheduling.annotation.Scheduled)";

    private static final String MID_BEGIN = "spring.scheduling.task.begin";
    private static final String AKEY_CLASS_NAME = "className";
    private static final String AKEY_METHOD_NAME = "methodName";
    private static final String AKEY_DURATION = "duration";

    private static final String MID_END = "spring.scheduling.task.end";

    public static final String PRTY_TRAIL_END_MODE = "trailMetrics.scheduling.endMode";
    public static final String PRTY_DISPATCH_BEGIN = "trailMetrics.scheduling.dispatchBegin";
    public static final String PRTY_DISPATCH_END = "trailMetrics.scheduling.dispatchEnd";
    public static final String DEFAULT_TRAIL_END_MODE = "IMMEDIATE";
    public static final boolean DEFAULT_DISPATCH_BEGIN = false;
    public static final boolean DEFAULT_DISPATCH_END = false;

    private SchedulingTrailEndMode mode;
    private boolean dispatchBeginTask;
    private boolean dispatchEndTask;

    /**
     * Default constructor.
     * <p>
     * Uses {@link SchedulingTrailEndMode#IMMEDIATE}
     */
    public TrailMetricsSchedulingInterceptor() {
        this(SchedulingTrailEndMode.IMMEDIATE, DEFAULT_DISPATCH_BEGIN, DEFAULT_DISPATCH_END);
    }

    /**
     * Advanced constructor.
     *
     * @param mode The mode to end trails with; may <b>not</b> be null.
     */
    public TrailMetricsSchedulingInterceptor(SchedulingTrailEndMode mode) {
        this(mode, DEFAULT_DISPATCH_BEGIN, DEFAULT_DISPATCH_END);
    }

    /**
     * Advanced constructor.
     *
     * @param mode The mode to end trails with; may <b>not</b> be null.
     * @param dispatchBeginTask Whether or not to write a metric when a task begins.
     * @param dispatchEndTask Whether or not to write a metric when a task ends.
     */
    @Autowired
    public TrailMetricsSchedulingInterceptor(@Value("${"+PRTY_TRAIL_END_MODE+":"+DEFAULT_TRAIL_END_MODE+"}") String mode,
                                             @Value("${"+PRTY_DISPATCH_BEGIN+":"+ DEFAULT_DISPATCH_BEGIN +"}") boolean dispatchBeginTask,
                                             @Value("${"+PRTY_DISPATCH_END+":"+ DEFAULT_DISPATCH_END +"}") boolean dispatchEndTask) {
        this(SchedulingTrailEndMode.valueOf(mode), dispatchBeginTask, dispatchEndTask);
    }

    /**
     * Advanced constructor.
     *
     * @param mode The mode to end trails with; may <b>not</b> be null.
     * @param dispatchBeginTask Whether or not to write a metric when a task begins.
     * @param dispatchEndTask Whether or not to write a metric when a task ends.
     */
    public TrailMetricsSchedulingInterceptor(SchedulingTrailEndMode mode, boolean dispatchBeginTask, boolean dispatchEndTask) {
        setMode(mode);
        this.dispatchBeginTask = dispatchBeginTask;
        this.dispatchEndTask = dispatchEndTask;
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

    /**
     * Returns whether to dispatch a metric when a task begins its run.
     *
     * @return True if a metric is dispatched, false otherwise.
     */
    public boolean isDispatchBeginTask() {
        return this.dispatchBeginTask;
    }

    /**
     * Sets whether to dispatch a metric when a task begins its run.
     *
     * @param dispatchBeginTask True if a metric should be dispatched, false otherwise.
     */
    public void setDispatchBeginTask(boolean dispatchBeginTask) {
        this.dispatchBeginTask = dispatchBeginTask;
    }

    /**
     * Returns whether to dispatch a metric when a task ends its run.
     *
     * @return True if a metric is dispatched, false otherwise.
     */
    public boolean isDispatchEndTask() {
        return dispatchEndTask;
    }

    /**
     * Sets whether to dispatch a metric when a task ends its run.
     *
     * @param dispatchEndTask True if a metric should be dispatched, false otherwise.
     */
    public void setDispatchEndTask(boolean dispatchEndTask) {
        this.dispatchEndTask = dispatchEndTask;
    }

    @Before(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void before(JoinPoint joinPoint) {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
        MetricsTrailSupport.begin();

        if (this.dispatchBeginTask) {
            Event event = new Event(MID_BEGIN,
                    new Measurement(AKEY_CLASS_NAME, joinPoint.getSignature().getDeclaringTypeName(), MeasurementType.STRING),
                    new Measurement(AKEY_METHOD_NAME, joinPoint.getSignature().getName(), MeasurementType.STRING));
            MetricsTrailSupport.commit(event, false);
        }

        TASK_DURATION.set(System.currentTimeMillis());
    }

    @AfterReturning(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void afterReturning() {
        dispatchEndMetric();
        if ((this.mode == SchedulingTrailEndMode.IMMEDIATE || this.mode == SchedulingTrailEndMode.IMMEDIATE_ON_SUCCESS)
                && MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }

    @AfterThrowing(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void afterThrowing() {
        dispatchEndMetric();
        if ((this.mode == SchedulingTrailEndMode.IMMEDIATE || this.mode == SchedulingTrailEndMode.IMMEDIATE_ON_FAILURE)
                && MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }

    private void dispatchEndMetric() {
        if (this.dispatchEndTask) {
            MetricsTrailSupport.commit(new Event(MID_END,
                    new Measurement(
                            AKEY_DURATION,
                            String.valueOf(System.currentTimeMillis()-TASK_DURATION.get()),
                            MeasurementType.LONG
                    )), false);
        }
        TASK_DURATION.set(null);
    }
}
