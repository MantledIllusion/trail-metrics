package com.mantledillusion.metrics.trail.api;

import java.util.function.Consumer;

import com.mantledillusion.metrics.trail.MetricsConsumer;
import com.mantledillusion.metrics.trail.MetricValidator;

/**
 * A type for {@link Metric}s. Different types might be interpreted differently
 * by a {@link MetricsConsumer}.
 */
public enum MetricType {

	/**
	 * Simple metric that is destined to alert about a certain event occurring.
	 * <p>
	 * For example, an {@link #ALERT} might indicate that at that moment a specific
	 * product was put into the shopping cart.
	 * <p>
	 * Operand is optional and has no enforced type.
	 */
	ALERT(MetricValidator.VALIDATOR_NOOP),

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
	PHASE(MetricValidator.VALIDATOR_NOOP),

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
	METER(MetricValidator.VALIDATOR_NUMERIC_OPERATOR),

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
	TREND(MetricValidator.VALIDATOR_NUMERIC_OPERATOR);

	private final Consumer<Metric> consumer;

	MetricType(Consumer<Metric> consumer) {
		this.consumer = consumer;
	}

	public void validateOperator(Metric metric) {
		this.consumer.accept(metric);
	}
}
