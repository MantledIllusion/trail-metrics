package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

import java.util.UUID;

/**
 * A consumer for {@link Event}s.
 */
@FunctionalInterface
public interface MetricsConsumer {

	/**
	 * Consumes a {@link Event}.
	 * 
	 * @param consumerId
	 *            The id of this consumer's registration that lead to the consumer  being called; might <b>not</b> be null.
	 * @param correlationId
	 *            The {@link UUID} of the trail the metric occurred in; never null.
	 * @param event
	 *            The dispatched {@link Event} to consume; might <b>not</b> be null.
	 * @throws Exception
	 *             Any {@link Exception} that might be thrown during consuming. Throwing an {@link Exception} will
	 *             cause the method to be called again later for a retry.
	 */
	void consume(String consumerId, UUID correlationId, Event event) throws Exception;
}
