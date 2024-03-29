package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.web.WebMetricRequest;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A receiver for {@link WebMetricRequest}s that is able to deliver them to registered {@link MetricsConsumer}s.
 */
public class MetricsReceiver {

    private class MatchedMetricsTrailConsumer {

        private final String matcher;
        private final MetricsTrailConsumer consumer;

        private MatchedMetricsTrailConsumer(String matcher, MetricsTrailConsumer consumer) {
            this.matcher = matcher;
            this.consumer = consumer;
        }

        private MetricsTrailConsumer.MetricsTrailConsumerQueue queueFor(UUID correlationId) {
            return this.consumer.queueFor(correlationId);
        }
    }

    private final Set<MatchedMetricsTrailConsumer> consumers = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Adds a {@link MetricsConsumer} to this {@link MetricsReceiver} that will
     * receive all events the receiver gets aware of.
     *
     * @param senderConsumerMatcher An as {@link Pattern} parsable {@link String}
     *                              matcher for the consumer ID the
     *                              {@link MetricsSender} is registered under on the
     *                              client side; might <b>not</b> be null.
     * @param consumerId            The id to add the consumer under, which will be
     *                              delivered to the consumer on each
     *                              {@link MetricsConsumer#consume(String, UUID, Event)}
     *                              invocation. Allows the same consumer to be
     *                              registered multiple times with differing
     *                              configurations; might <b>not</b> be null.
     * @param consumer              The consumer to add; might <b>not</b> be null.
     * @return The created {@link MetricsTrailConsumer} that can be used to configure the consumer, never null
     */
    public MetricsTrailConsumer addConsumer(String senderConsumerMatcher, String consumerId, MetricsConsumer consumer) {
        return addConsumer(senderConsumerMatcher, consumerId, consumer, null, null);
    }

    /**
     * Adds a {@link MetricsConsumer} to this {@link MetricsReceiver} that will
     * receive all events the receiver gets aware of.
     *
     * @param senderConsumerMatcher An as {@link Pattern} parsable {@link String}
     *                              matcher for the consumer ID the
     *                              {@link MetricsSender} is registered under on the
     *                              client side; might <b>not</b> be null.
     * @param consumerId            The unique id to add the consumer under, which
     *                              will be delivered to the consumer on each
     *                              {@link MetricsConsumer#consume(String, UUID, Event)}
     *                              invocation. Allows the same consumer to be
     *                              registered multiple times with differing
     *                              configurations; might <b>not</b> be null.
     * @param consumer              The consumer to add; might <b>not</b> be null.
     * @param gate                  The predicate that needs to
     *                              {@link MetricsPredicate#test(Event)} true
     *                              to trigger flushing all of a session's
     *                              accumulated {@link Event}s; might be null.
     * @param filter                The predicate that needs to
     *                              {@link MetricsPredicate#test(Event)} true
     *                              to allow an about-to-be-flushed event to be
     *                              delivered to the consumer; might be null.
     * @return The created {@link MetricsTrailConsumer} that can be used to configure the consumer, never null
     */
    public synchronized MetricsTrailConsumer addConsumer(String senderConsumerMatcher, String consumerId,
                                                         MetricsConsumer consumer, MetricsPredicate gate, MetricsPredicate filter) {
        if (senderConsumerMatcher == null) {
            throw new IllegalArgumentException("Cannot add a consumer using a null sender consumer matcher");
        }
        MetricsTrailConsumer trailConsumer = MetricsTrailConsumer.from(consumerId, consumer, gate, filter);
        this.consumers.add(new MatchedMetricsTrailConsumer(senderConsumerMatcher, trailConsumer));
        return trailConsumer;
    }

    /**
     * Unpacks the given packaged {@link Event}s and dispatches them to this
     * receiver's {@link MetricsConsumer}s.
     *
     * @param request The request to unpack and disptach; might <b>not</b> be null.
     */
    public synchronized void receive(WebMetricRequest request) {
        // For each consumer...
        this.consumers.parallelStream().forEach(consumer -> {
            // ...go over the request's consumers...
            request.getConsumers().parallelStream().forEach(requestConsumer -> {
                // ...and if the request consumer's ID matches the consumer's one...
                if (requestConsumer.getConsumerId().matches(consumer.matcher)) {
                    // ...go over the request consumer's trails...
                    requestConsumer.getTrails().parallelStream().forEach(trail -> {
                        UUID correlationId = UUID.fromString(trail.getCorrelationId());
                        MetricsTrailConsumer.MetricsTrailConsumerQueue queue = consumer.queueFor(correlationId);
                        // ...and that trail's metrics...
                        trail.getMetrics().stream().forEach(metric -> {
                            // ...enqueue them to the queue...
                            queue.enqueue(metric.to());
                        });
                        // ...and then end the trail
                        queue.onTrailEnd();
                    });
                }
            });
        });
    }
}
