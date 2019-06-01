package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

/**
 * A stateful valve for {@link Metric}s.
 * <p>
 * A {@link MetricsValve} is able to cause a {@link MetricsTrailConsumer} to hold all
 * of a trail's events back until a {@link Metric} occurs that flushes
 * the accumulated events.
 * <p>
 * In contract to a {@link MetricsPredicate}, a valve is stateful, so it stays
 * open after it has been opened by a {@link Metric} once.
 */
public final class MetricsValve implements MetricsPredicate {

	private final MetricsPredicate predicate;
	private boolean isOpen;

	private MetricsValve(MetricsPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(Metric metric) {
		if (!this.isOpen) {
			this.isOpen = this.predicate.test(metric);
		}
		return this.isOpen;
	}

	/**
	 * Returns whether this {@link MetricsValve} is open.
	 *
	 * @return True if this valve is open, false otherwise
	 */
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public MetricsValve functionalClone() {
		return new MetricsValve(this.predicate);
	}

	/**
	 * Turns the given (possibly stateless) {@link MetricsPredicate} into a stateful
	 * {@link MetricsValve}.
	 * 
	 * @param predicate The predicate to {@link MetricsPredicate#test(Metric)}
	 *                  in the new {@link MetricsValve}; might <b>not</b> be null.
	 * @return A new {@link MetricsValve} containing the given
	 *         {@link MetricsPredicate} for its
	 *         {@link MetricsPredicate#test(Metric)}, never null
	 */
	public static MetricsValve of(MetricsPredicate predicate) {
		if (predicate == null) {
			throw new IllegalArgumentException("Cannot create a valve from a null predicate");
		}
		return new MetricsValve(predicate);
	}
}