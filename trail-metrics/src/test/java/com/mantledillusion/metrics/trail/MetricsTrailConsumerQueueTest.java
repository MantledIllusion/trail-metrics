package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsTrailConsumerQueueTest extends AbstractMetricsTest {

    private MetricsTrail trail;

    @BeforeEach
    public void beginTrail() {
        this.trail = new MetricsTrail(TRAIL_ID);
    }

    @Test
    public void testBasicDelivery() {
        this.queue = this.trail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        Event eventA = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(eventA);
        Event eventB = new Event(TEST_EVENT_PREFIX+"B");
        this.trail.commit(eventB);
        Event eventC = new Event(TEST_EVENT_PREFIX+"C");
        this.trail.commit(eventC);
        waitUntilConsumed();

        assertEquals(3, this.consumer.size(TRAIL_ID));
        assertSame(eventA, this.consumer.dequeueOne(TRAIL_ID));
        assertSame(eventB, this.consumer.dequeueOne(TRAIL_ID));
        assertSame(eventC, this.consumer.dequeueOne(TRAIL_ID));
    }

    @Test
    public void testHookedToCorrectTrail() {
        this.queue = this.trail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        assertEquals(TRAIL_ID, queue.getCorrelationId());
    }

    @Test
    public void testDelayedDelivering() {
        this.queue = this.trail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Event eventA = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(eventA);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        Event eventB = new Event(TEST_EVENT_PREFIX+"B");
        this.trail.commit(eventB);

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
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer);
        consumer.setDeliveryRetryIntervals(5);
        this.queue = this.trail.hook(consumer);

        this.consumer.breakConsumer();
        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Event eventA = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(eventA);

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
        this.queue = this.trail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer));

        this.consumer.breakSystem();
        this.consumer.block();

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());

        Event eventA = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(eventA);

        assertTrue(this.queue.isDelivering());
        assertEquals(1, this.queue.getDeliveringCount());

        this.consumer.unblockOne();
        waitUntilFailed(1);

        assertFalse(this.queue.isDelivering());
        assertEquals(0, this.queue.getDeliveringCount());
    }

    @Test
    public void testNoFlushOnTrailEnd() {
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer, metric -> false, null);
        this.queue = this.trail.hook(consumer);

        assertFalse(consumer.doFlushOnTrailEnd());

        Event eventA = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(eventA);

        assertTrue(this.queue.hasGated());
        assertEquals(1, this.queue.getGatedCount());

        this.trail.end();

        assertFalse(this.queue.hasGated());
        assertEquals(0, this.queue.getGatedCount());

        waitUntilConsumed();

        assertEquals(0, this.consumer.size(TRAIL_ID));
    }

    @Test
    public void testDoFlushOnTrailEnd() {
        MetricsTrailConsumer consumer = MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer, metric -> false, null);
        consumer.setDoFlushOnTrailEnd(true);
        this.queue = this.trail.hook(consumer);

        assertTrue(consumer.doFlushOnTrailEnd());

        Event event = new Event(TEST_EVENT_PREFIX+"A");
        this.trail.commit(event);

        assertTrue(this.queue.hasGated());
        assertEquals(1, this.queue.getGatedCount());

        this.trail.end();

        assertFalse(this.queue.hasGated());
        assertEquals(0, this.queue.getGatedCount());

        waitUntilConsumed();

        assertEquals(1, this.consumer.size(TRAIL_ID));
        assertSame(event, this.consumer.dequeueOne(TRAIL_ID));
    }
}
