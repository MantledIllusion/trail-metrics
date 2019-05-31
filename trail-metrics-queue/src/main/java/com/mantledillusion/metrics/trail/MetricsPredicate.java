package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

/**
 * A stateless predicate for {@link Metric}s.
 * <p>
 * A {@link MetricsPredicate} is able to cause a {@link MetricsTrailConsumer}
 * to hold all of a trail's events back until a {@link Metric} occurs that
 * flushes the accumulated events.
 * <p>
 * In contract to a {@link MetricsValve}, a predicate is stateless, so it reacts
 * on every {@link Metric} individually and might close again after it has been
 * opened.
 */
@FunctionalInterface
public interface MetricsPredicate {

	/**
	 * Tests whether the given event matched this predicate.
	 * 
	 * @param metric The event to test; might <b>not</b> be null.
	 * @return True if the predicate is fulfilled, false otherwise.
	 */
	boolean test(Metric metric);

	/**
	 * Returns a clone of this predicate that reacts the same as this predicate
	 * does, but is stateless, so the state it might possibly have is not cloned.
	 * 
	 * @return A functional clone, never null
	 */
	default MetricsPredicate functionalClone() {
		return this;
	}

	/**
	 * Combines this predicate with the given one to an AND conjunction.
	 * <p>
	 * Functionally equals the Java {@code &&} modifier.
	 * 
	 * @param other The other predicate to combine with; might <b>not</b> be null.
	 * @return A new {@link MetricsPredicate} instance, containing both this and the
	 *         given predicate for its {@link #test(Metric)}, never null
	 */
	default MetricsPredicate and(MetricsPredicate other) {
		if (other == null) {
			throw new IllegalArgumentException("Cannot create an AND conjunction with a null second predicate");
		}
		MetricsPredicate base = this;
		return new MetricsPredicate() {

			@Override
			public boolean test(Metric metric) {
				return base.test(metric) && other.test(metric);
			}

			@Override
			public MetricsPredicate functionalClone() {
				MetricsPredicate baseClone = base.functionalClone();
				MetricsPredicate otherClone = other.functionalClone();
				return baseClone.and(otherClone);
			}
		};
	}

	/**
	 * Combines this predicate with the given one to an OR conjunction.
	 * <p>
	 * Functionally equals the Java {@code ||} modifier.
	 * 
	 * @param other The other predicate to combine with; might <b>not</b> be null.
	 * @return A new {@link MetricsPredicate} instance, containing both this and the
	 *         given predicate for its {@link #test(Metric)}, never null
	 */
	default MetricsPredicate or(MetricsPredicate other) {
		if (other == null) {
			throw new IllegalArgumentException("Cannot create an OR conjunction with a null second predicate");
		}
		MetricsPredicate base = this;
		return new MetricsPredicate() {

			@Override
			public boolean test(Metric metric) {
				return base.test(metric) || other.test(metric);
			}

			@Override
			public MetricsPredicate functionalClone() {
				MetricsPredicate baseClone = base.functionalClone();
				MetricsPredicate otherClone = other.functionalClone();
				return baseClone.or(otherClone);
			}
		};
	}

	/**
	 * Turns this (possibly stateless) {@link MetricsPredicate} into a stateful
	 * {@link MetricsValve}.
	 * 
	 * @return A new {@link MetricsValve} containing this {@link MetricsPredicate}
	 *         for its {@link #test(Metric)}, never null
	 */
	default MetricsValve asValve() {
		return MetricsValve.of(this);
	}
}