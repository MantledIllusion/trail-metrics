package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

public abstract class AbstractMetricsTest {

    protected static final UUID TRAIL_ID = UUID.randomUUID();

    protected static final String TEST_EVENT_PREFIX = "test.";
    protected static final String TEST_IMPORTANT_EVENT_PREFIX = TEST_EVENT_PREFIX+"important.";
    protected static final String TEST_CONSUMER = "testConsumer";

    protected MockConsumer consumer;
    protected MetricsTrailConsumer.MetricsTrailConsumerQueue queue;

    @BeforeEach
    public void createConsumer() {
        this.consumer = new MockConsumer();
    }

    protected void waitUntilConsumed() {
        waitUntilConsumed(0);
    }

    protected void waitUntilConsumed(int expectedDeliveringCount) {
        while (this.queue.getDeliveringCount() > expectedDeliveringCount) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for event consuming");
            }
        }
    }

    protected void waitUntilFailed(int expectedFailCount) {
        while (this.consumer.fails(this.queue.getTrailId()) < expectedFailCount) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to wait for event consuming");
            }
        }
    }
}
