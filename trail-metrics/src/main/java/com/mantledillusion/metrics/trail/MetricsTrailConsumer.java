package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;
import java.util.concurrent.*;

/**
 * Represents a {@link MetricsConsumer} that can consume {@link Metric}s from a {@link MetricsTrail}.
 */
public final class MetricsTrailConsumer {

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

    /**
     * Represents a queue that retrieves {@link Metric}s from a {@link MetricsTrail} to deliver them to a {@link MetricsTrailConsumer}.
     */
    public class MetricsTrailConsumerQueue {

        private class LinkedMetric {

            private final Metric metric;
            private LinkedMetric next;

            private LinkedMetric(Metric metric) {
                this.metric = metric;
            }

            private void delivered() {
                synchronized (MetricsTrailConsumerQueue.this) {
                    if (MetricsTrailConsumerQueue.this.first != null) {
                        MetricsTrailConsumerQueue.this.first = this.next;
                        if (MetricsTrailConsumerQueue.this.first == null) {
                            MetricsTrailConsumerQueue.this.last = null;
                        }
                    }
                }
            }

            private void failed() {
                MetricsTrailConsumerQueue.this.clearQueue();
            }
        }

        private final UUID trailId;
        private final MetricsPredicate gate;
        private final MetricsPredicate filter;

        private LinkedMetric first;
        private LinkedMetric current;
        private LinkedMetric last;

        private MetricsTrailConsumerQueue(UUID trailId) {
            this.trailId = trailId;
            this.gate = MetricsTrailConsumer.this.gate != null ? MetricsTrailConsumer.this.gate.functionalClone() : null;
            this.filter = MetricsTrailConsumer.this.filter != null ? MetricsTrailConsumer.this.filter.functionalClone() : null;
        }

        synchronized void enqueue(Metric metric) {
            if (this.filter == null || this.filter.test(metric)) {
                LinkedMetric linkedMetric = new LinkedMetric(metric);
                if (this.first == null) {
                    this.first = linkedMetric;
                }
                if (this.current == null) {
                    this.current = linkedMetric;
                }
                if (this.last != null) {
                    this.last.next = linkedMetric;
                }
                this.last = linkedMetric;
            }
            if (this.gate == null || this.gate.test(metric)) {
                deliverAccumulated();
            }
        }

        synchronized void onTrailEnd() {
            if (MetricsTrailConsumer.this.doFlushOnTrailEnd) {
                deliverAccumulated();
            }
            clearQueue();
        }

        private void clearQueue() {
            synchronized (MetricsTrailConsumerQueue.this) {
                MetricsTrailConsumerQueue.this.first = null;
                MetricsTrailConsumerQueue.this.current = null;
                MetricsTrailConsumerQueue.this.last = null;
            }
        }

        private synchronized void deliverAccumulated() {
            while (this.current != null) {
                LinkedMetric linkedMetric = this.current;
                this.current = this.current.next;
                MetricsTrailConsumer.this.deliverHead(this.trailId, linkedMetric);
            }
        }

        /**
         * Returns the ID of the trail this queue retrieves metrics from.
         *
         * @return The trail's ID, never null
         */
        public UUID getTrailId() {
            return this.trailId;
        }

        /**
         * Returns whether there are {@link Metric}s that are enqueued and waiting for this consumer's gate to open so they can be delivered.
         *
         * @return True if there is at least one {@link Metric} currently gated, false otherwise
         */
        public synchronized boolean hasGated() {
            return this.current != null;
        }

        /**
         * Returns the count of {@link Metric}s that are enqueued and waiting for this consumer's gate to open so they can be delivered.
         *
         * @return The count of {@link Metric}s currently gated
         */
        public synchronized int getGatedCount() {
            int count = 0;
            LinkedMetric current = this.current;
            while (current != null) {
                count++;
                current = current.next;
            }
            return count;
        }

        /**
         * Returns whether there are {@link Metric}s that are currently being delivered to this consumer by asynchronous tasks.
         *
         * @return True if there is at least one {@link Metric} currently being delivered, false otherwise
         */
        public synchronized boolean isDelivering() {
            return this.first != this.current;
        }

        /**
         * Returns the count of {@link Metric}s that are currently being delivered to this consumer by asynchronous tasks.
         *
         * @return The count of {@link Metric}s currently being delivered
         */
        public synchronized int getDeliveringCount() {
            int count = 0;
            LinkedMetric current = this.first;
            while (current != null && current != this.current) {
                count++;
                current = current.next;
            }
            return count;
        }
    }

    private final String consumerId;
    private final MetricsConsumer consumer;

    private final MetricsPredicate gate;
    private final MetricsPredicate filter;

    private long[] consumerRetryIntervals = CONSUMER_DELIVERY_RETRY_INTERVALS;
    private boolean doFlushOnTrailEnd = false;

    private final ThreadPoolExecutor delivererService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    private MetricsTrailConsumer(String consumerId, MetricsConsumer consumer, MetricsPredicate gate, MetricsPredicate filter) {
        this.consumerId = consumerId;
        this.consumer = consumer;
        this.gate = gate != null ? gate.functionalClone() : null;
        this.filter = filter != null ? filter.functionalClone() : null;
    }

    private synchronized void deliverHead(UUID trailId, MetricsTrailConsumerQueue.LinkedMetric linkedMetric) {
        if (!MetricsTrailConsumer.this.delivererService.isShutdown()) {
            MetricsTrailConsumer.this.delivererService.execute(() -> {
                int tries = 0;
                while (true) {
                    try {
                        MetricsTrailConsumer.this.consumer.consume(MetricsTrailConsumer.this.consumerId, trailId, linkedMetric.metric);
                        linkedMetric.delivered();
                        break;
                    } catch (Exception e) {
                        /*
                         * If a consumer is not able to consume its delivery, we wait for the next time
                         * to try it.
                         */
                        tries = awaitRetry(linkedMetric, tries);
                    } catch (Throwable t) {
                        /*
                         * When something so destructive happens, we unregister the consumer to make
                         * sure not to create inconsistent data
                         */
                        shutdown(linkedMetric);
                        throw t;
                    }
                }
            });
        }
    }

    private int awaitRetry(MetricsTrailConsumerQueue.LinkedMetric linkedMetric, int tries) {
        try {
            long retryIntervalMs = MetricsTrailConsumer.this.consumerRetryIntervals[tries];

            Thread.sleep(retryIntervalMs);
            return Math.min(tries + 1, MetricsTrailConsumer.this.consumerRetryIntervals.length - 1);
        } catch (Exception e) {
            /*
             * If we are not able to wait for a next try we cannot continue; we unregister
             * the consumer to make sure not to create inconsistent data
             */
            shutdown(linkedMetric);
            throw new RuntimeException("Delivering a metric to the " + MetricsConsumer.class.getSimpleName()
                    + " '" + MetricsTrailConsumer.this.consumer
                    + "' failed, and triggering to wait for a retry failed as well.", e);
        }
    }

    private synchronized void shutdown(MetricsTrailConsumerQueue.LinkedMetric linkedMetric) {
        linkedMetric.failed();
        MetricsTrailConsumer.this.delivererService.shutdownNow();
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
        consumerRetryIntervals = new long[intervals.length + 1];
        Arrays.setAll(consumerRetryIntervals, i -> i == 0 ? interval : intervals[i - 1]);
        if (Arrays.stream(consumerRetryIntervals).anyMatch(i -> i < 0)) {
            throw new IllegalArgumentException("Cannot set a retry interval < 0");
        }
        this.consumerRetryIntervals = consumerRetryIntervals;
    }

    /**
     * Returns whether the {@link MetricsTrailConsumer} should flush all of a trail's gated events when that trail ends,
     * no matter whether the gate has been opened.
     *
     * @return True if a possibly set gate should be ignored when a session is
     *         destroyed, false otherwise.
     */
    public boolean doFlushOnTrailEnd() {
        return doFlushOnTrailEnd;
    }

    /**
     * Sets whether the {@link MetricsTrailConsumer} should flush all of a trail's gated events when that trail ends,
     * no matter whether the gate has been opened.
     * <p>
     * False by default.
     *
     * @param doFlushOnTrailEnd True if a possibly set gate should be ignored
     *                                when a session is destroyed, false otherwise.
     */
    public void setDoFlushOnTrailEnd(boolean doFlushOnTrailEnd) {
        this.doFlushOnTrailEnd = doFlushOnTrailEnd;
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
