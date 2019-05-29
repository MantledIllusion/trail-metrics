package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.UUID;

/**
 * A consumer for {@link Metric}s.
 */
@FunctionalInterface
public interface MetricsConsumer {

	/**
	 * Consumes a {@link Metric}.
	 * 
	 * @param consumerId
	 *            The id of this consumer's registration that lead to the consumer
	 *            being called; might <b>not</b> be null.
	 * @param trailId
	 *            The {@link UUID} of the trail the metric occurred in; never null.
	 * @param metric
	 *            The dispatched {@link Metric} to consume; might <b>not</b> be
	 *            null.
	 * @throws Exception
	 *             Any {@link Exception} that might be thrown during consuming.
	 *             Throwing an {@link Exception} will cause the method to be called
	 *             again later for a retry.
	 */
	void consume(String consumerId, UUID trailId, Metric metric) throws Exception;
}
