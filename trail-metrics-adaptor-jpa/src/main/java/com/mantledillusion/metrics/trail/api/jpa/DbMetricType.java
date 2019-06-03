package com.mantledillusion.metrics.trail.api.jpa;

/**
 * A type for {@link DbMetric}s. Different types might be interpreted differently
 * by a {@link com.mantledillusion.metrics.trail.MetricsConsumer}.
 */
public enum DbMetricType {

	/**
	 * Simple metric that is destined to alert about a certain event occurring.
	 * <p>
	 * For example, an {@link #ALERT} might indicate that at that moment a specific
	 * product was put into the shopping cart.
	 * <p>
	 * Operand is optional and has no enforced type.
	 */
	ALERT,

	/**
	 * Metric that marks the begin of a new phase in a sequence of events with the
	 * same category.
	 * <p>
	 * For example, a {@link #PHASE} might indicate that from this moment on the
	 * user was logged in with specific credentials, while the next phases begin
	 * when the user logs out, logs in with different credentials again, etc. A
	 * phase is never actively closed, it ends automatically when the next phase
	 * begins.
	 * <p>
	 * Operand is optional and has no enforced type.
	 */
	PHASE,

	/**
	 * Metric that signals an absolute magnitude of a specific value at the moment
	 * the metric is created.
	 * <p>
	 * It is favorable for measurements where changes of a specific value cannot be
	 * observed, but the current overall value can be looked up.
	 * <p>
	 * For example, a {@link #METER} might be periodically created to indicate that
	 * there were {@code x} open database connections in the connection pool, or the
	 * injection duration of a specific view was {@code y} milliseconds.
	 * <p>
	 * Operand is mandatory and has to be a {@link Float} value. It will be directly
	 * used as the current value.
	 */
	METER,

	/**
	 * Metric that signals a relative change to a specific value at the moment the
	 * metric is created, with 0 being the initial value.
	 * <p>
	 * It is favorable for measurements where changes of a specific value can easily
	 * be observed, but the current overall value is hard to determine.
	 * <p>
	 * For example, a {@link #TREND} might be +1 increased every time a user uses a
	 * 'help me' functionality or -/+1 adjusted when the user claims a help tip is
	 * useful or not.
	 * <p>
	 * Operand is mandatory and has to be a {@link Float} value. It will be added to
	 * the current value.
	 */
	TREND;
}
