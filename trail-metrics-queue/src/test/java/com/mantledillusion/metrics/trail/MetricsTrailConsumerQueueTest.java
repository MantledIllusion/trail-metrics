package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsTrailConsumerQueueTest extends AbstractMetricsTest {

    @Test
    public void testBasicDelivery() {
        MetricsTrail.begin(TRAIL_ID);
        this.queue = MetricsTrail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metricA);
        Metric metricB = new Metric(TEST_EVENT_PREFIX+"B", MetricType.ALERT);
        MetricsTrail.commit(metricB);
        Metric metricC = new Metric(TEST_EVENT_PREFIX+"C", MetricType.ALERT);
        MetricsTrail.commit(metricC);
        waitUntilConsumed();

        assertEquals(3, this.consumer.size(TRAIL_ID));
        assertSame(metricA, this.consumer.dequeueOne(TRAIL_ID));
        assertSame(metricB, this.consumer.dequeueOne(TRAIL_ID));
        assertSame(metricC, this.consumer.dequeueOne(TRAIL_ID));
    }

    @Test
    public void testHookedToCorrectTrail() {
        MetricsTrail.begin(TRAIL_ID);
        this.queue = MetricsTrail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        assertEquals(TRAIL_ID, queue.getTrailId());
    }

    @Test
    public void testDelayedDelivering() {
        MetricsTrail.begin(TRAIL_ID);
        this.queue = MetricsTrail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metricA);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        Metric metricB = new Metric(TEST_EVENT_PREFIX+"B", MetricType.ALERT);
        MetricsTrail.commit(metricB);

        assertTrue(this.queue.isDelivering());
        assertEquals(2, this.queue.getDeliveringCount());

        this.consumer.unblockOne();
        waitUntilConsumed(1);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        this.consumer.unblockOne();
        waitUntilConsumed(0);

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());
    }

    @Test
    public void testRetryDelivering() {
        MetricsTrail.begin(TRAIL_ID);
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer);
        consumer.setDeliveryRetryIntervals(5);
        this.queue = MetricsTrail.hook(consumer);

        this.consumer.breakConsumer();
        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metricA);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        this.consumer.unblockOne();
        waitUntilFailed(1);

        this.consumer.unblockOne();
        waitUntilFailed(2);

        this.consumer.healConsumer();

        this.consumer.unblockOne();
        waitUntilConsumed();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());
    }

    @Test
    public void testDeliverySystemFailure() {
        MetricsTrail.begin(TRAIL_ID);
        this.queue = MetricsTrail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        this.consumer.breakSystem();
        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metricA);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        this.consumer.unblockOne();
        waitUntilFailed(1);

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());
    }

    @Test
    public void testNoFlushOnTrailEnd() {
        MetricsTrail.begin(TRAIL_ID);
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer, metric -> false, null);
        this.queue = MetricsTrail.hook(consumer);

        assertFalse(consumer.doFlushOnTrailEnd());

        Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metricA);

        assertTrue(this.queue.hasGated());
        assertEquals(1, this.queue.getGatedCount());

        MetricsTrail.end();

        assertFalse(this.queue.hasGated());
        assertEquals(0, this.queue.getGatedCount());

        waitUntilConsumed();

        assertEquals(0, this.consumer.size(TRAIL_ID));
    }

    @Test
    public void testDoFlushOnTrailEnd() {
        MetricsTrail.begin(TRAIL_ID);
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer, metric -> false, null);
        consumer.setDoFlushOnTrailEnd(true);
        this.queue = MetricsTrail.hook(consumer);

        assertTrue(consumer.doFlushOnTrailEnd());

        Metric metric = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
        MetricsTrail.commit(metric);

        assertTrue(this.queue.hasGated());
        assertEquals(1, this.queue.getGatedCount());

        MetricsTrail.end();

        assertFalse(this.queue.hasGated());
        assertEquals(0, this.queue.getGatedCount());

        waitUntilConsumed();

        assertEquals(1, this.consumer.size(TRAIL_ID));
        assertSame(metric, this.consumer.dequeueOne(TRAIL_ID));
    }
}
