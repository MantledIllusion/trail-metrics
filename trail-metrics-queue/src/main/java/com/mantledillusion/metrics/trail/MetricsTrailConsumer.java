package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Represents a {@link MetricsConsumer} that can consume {@link Metric}s from a {@link MetricsTrail}.
 */
public class MetricsTrailConsumer {

    private static final long[] CONSUMER_DELIVERY_RETRY_INTERVALS = {
            // 5 Seconds
            5000,
            // 1 Minute
            60000,
            // 5 Minutes
            300000,
            // 15 Minutes
            900000,
            // 30 Minutes
            1800000};

    private long[] consumerRetryIntervals = CONSUMER_DELIVERY_RETRY_INTERVALS;

    class MetricsTrailConsumerQueue {

        private final UUID trailId;
        private final MetricsPredicate gate;
        private final MetricsPredicate filter;
        private final List<Metric> accumulatedDeliveries = new ArrayList<>();
        private final ThreadPoolExecutor delivererService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        private MetricsTrailConsumerQueue(UUID trailId) {
            this.trailId = trailId;
            this.gate = MetricsTrailConsumer.this.gate != null ? MetricsTrailConsumer.this.gate.functionalClone() : null;
            this.filter = MetricsTrailConsumer.this.filter != null ? MetricsTrailConsumer.this.filter.functionalClone() : null;
        }

        void enqueue(Metric metric) {
            this.accumulatedDeliveries.add(metric);
            if (this.gate == null || (metric != null && this.gate.test(metric))) {
                this.accumulatedDeliveries.stream()
                        .filter(accumulatedDelivery -> this.filter == null || this.filter.test(accumulatedDelivery))
                        .forEach(accumulatedDelivery -> MetricsTrailConsumerQueue.this.deliver(this.trailId, accumulatedDelivery));
                this.accumulatedDeliveries.clear();
            }
        }

        private void deliver(UUID trailId, Metric metric) {
            this.delivererService.execute(() -> {
                int tries = 0;
                try {
                    while (true) {
                        try {
                            MetricsTrailConsumer.this.consumer.consume(MetricsTrailConsumer.this.consumerId, trailId, metric);
                            break;
                        } catch (Exception e) {
                            /*
                             * If a consumer is not able to consume its delivery, we wait for the next time
                             * to try it.
                             */
                            long retryIntervalMs = MetricsTrailConsumer.this.consumerRetryIntervals[tries];

                            try {
                                onRetry(consumer, e, retryIntervalMs);
                            } catch (Exception e2) {
                                // nop: this method is just called to inform.
                            }

                            Thread.sleep(retryIntervalMs);
                            tries = Math.min(tries + 1, MetricsTrailConsumer.this.consumerRetryIntervals.length - 1);
                        }
                    }
                } catch (InterruptedException e) {
                    /*
                     * If we are not able to wait for a next try we cannot continue; we unregister
                     * the consumer to make sure not to create inconsistent data
                     */
                    remove();
                    throw new RuntimeException("Delivering a metric to the " + MetricsConsumer.class.getSimpleName()
                            + " '" + MetricsTrailConsumer.this.consumer
                            + "' failed, and triggering to wait for a retry failed as well.", e);
                } catch (Throwable t) {
                    /*
                     * When something so destructive happens, we unregister the consumer to make
                     * sure not to create inconsistent data
                     */
                    remove();
                    throw t;
                }
            });
        }
    }

    private final String consumerId;
    private final MetricsConsumer consumer;

    private final MetricsPredicate gate;
    private final MetricsPredicate filter;

    private MetricsTrailConsumer(String consumerId, MetricsConsumer consumer, MetricsPredicate gate, MetricsPredicate filter) {
        this.consumerId = consumerId;
        this.consumer = consumer;
        this.gate = gate != null ? gate.functionalClone() : null;
        this.filter = filter != null ? filter.functionalClone() : null;
    }

    MetricsTrailConsumerQueue queueFor(UUID trailId) {
        return new MetricsTrailConsumerQueue(trailId);
    }

    /**
     * Sets the intervals in milliseconds the {@link MetricsTrailConsumerQueue} waits
     * until it tries to redeliver a metric to a {@link MetricsConsumer} again after
     * the first delivery failed.
     * <p>
     * If the delivery fails more times than there are intervals set, the last
     * defined interval is used.
     * <p>
     * For example, if the method is called with the arguments (0, 5000, 300000),
     * the first retry will be done directly after the first failed, the second
     * after 5 seconds and the third-&gt;nth after 5 minutes.
     * <p>
     * The default intervals are {@link #CONSUMER_DELIVERY_RETRY_INTERVALS}.
     *
     * @param interval  The first interval; might <b>not</b> be negative.
     * @param intervals The additional intervals; might <b>not</b> be negative.
     */
    public void setDeliveryRetryIntervals(long interval, long... intervals) {
        long[] consumerRetryIntervals;
        if (intervals == null) {
            consumerRetryIntervals = new long[]{interval};
        } else {
            consumerRetryIntervals = new long[intervals.length + 1];
            Arrays.setAll(consumerRetryIntervals, i -> i == 0 ? interval : intervals[i - 1]);
        }
        if (Arrays.stream(consumerRetryIntervals).anyMatch(i -> i < 0)) {
            throw new IllegalArgumentException("Cannot set a retry interval < 0");
        }
        this.consumerRetryIntervals = consumerRetryIntervals;
    }

    /**
     * Creates a {@link MetricsTrailConsumer} that can consume all {@link Metric}s a {@link MetricsTrail} gets aware of.
     *
     * @param consumerId The id to add the consumer under, which will be delivered
     *                   to the consumer on each
     *                   {@link MetricsConsumer#consume(String, UUID, Metric)}
     *                   invocation. Allows the same consumer to be registered
     *                   multiple times with differing configurations; might
     *                   <b>not</b> be null.
     * @param consumer   The consumer to add; might <b>not</b> be null.
     * @return The created {@link MetricsTrailConsumer} that can be used to configure and
     * unregister the consumer, never null
     */
    public static MetricsTrailConsumer from(String consumerId, MetricsConsumer consumer) {
        return from(consumerId, consumer, null, null);
    }

    /**
     * Creates a {@link MetricsTrailConsumer} that can consume all {@link Metric}s a {@link MetricsTrail} gets aware of.
     *
     * @param consumerId The unique id to add the consumer under, which will be
     *                   delivered to the consumer on each
     *                   {@link MetricsConsumer#consume(String, UUID, Metric)}
     *                   invocation. Allows the same consumer to be registered
     *                   multiple times with differing configurations; might
     *                   <b>not</b> be null.
     * @param consumer   The consumer to add; might <b>not</b> be null.
     * @param gate       The predicate that needs to
     *                   {@link MetricsPredicate#test(Metric)} true to trigger
     *                   flushing all of a trail's accumulated
     *                   {@link Metric}s; might be null.
     * @param filter     The predicate that needs to
     *                   {@link MetricsPredicate#test(Metric)} true to allow an
     *                   about-to-be-flushed metric to be delivered to the consumer;
     *                   might be null.
     * @return The created {@link MetricsTrailConsumer} that can be used to configure and
     * unregister the consumer, never null
     */
    public static MetricsTrailConsumer from(String consumerId, MetricsConsumer consumer, MetricsPredicate gate, MetricsPredicate filter) {
        if (consumerId == null || consumerId.isEmpty()) {
            throw new IllegalArgumentException("Cannot register a consumer under a null or empty id");
        } else if (consumer == null) {
            throw new IllegalArgumentException("Cannot register a null consumer");
        }
        return new MetricsTrailConsumer(consumerId, consumer, gate, filter);
    }
}
