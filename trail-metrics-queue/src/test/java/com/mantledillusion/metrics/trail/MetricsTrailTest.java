package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MetricsTrailTest {

	private static final UUID TRAIL_ID = UUID.randomUUID();

	private static final String TEST_EVENT_PREFIX = "test.";
	private static final String TEST_IMPORTANT_EVENT_PREFIX = TEST_EVENT_PREFIX+"important.";

	private MockMetricsConsumer consumer;
	private MetricsTrailConsumer.MetricsTrailConsumerQueue queue;
	
	@BeforeEach
	public void beginTrail() {
		this.consumer = new MockMetricsConsumer();
		MetricsTrail.begin(TRAIL_ID);
	}

	@AfterEach
	public void endTrail() {
		MetricsTrail.end();
	}
	
	@Test
	public void testBasicDelivery() {
		this.queue = MetricsTrail.hook(MetricsTrailConsumer.from("testConsumer", this.consumer));

		Metric metric = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
		MetricsTrail.commit(metric);
		waitUntilConsumed();
		
		assertEquals(1, this.consumer.size(TRAIL_ID));
		assertSame(metric, this.consumer.dequeueOne(TRAIL_ID));
	}
	
	@Test
	public void testStatelessGate() {
		MetricsPredicate gate = event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX);
		int[][] expectedCounts = {
				new int[] {0, 0},
				new int[] {2, 0},
				new int[] {2, 0},
				new int[] {4, 0}};
		testPredicates(gate, null, expectedCounts);
	}
	
	@Test
	public void testStatefulGate() {
		MetricsPredicate gate = MetricsValve.of(event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX));
		int[][] expectedCounts = {
				new int[] {0, 0},
				new int[] {2, 0},
				new int[] {3, 0},
				new int[] {4, 0}};
		testPredicates(gate, null, expectedCounts);
	}
	
	@Test
	public void testStatelessFilter() {
		MetricsPredicate filter = event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX);
		int[][] expectedCounts = {
				new int[] {0, 0},
				new int[] {1, 0},
				new int[] {1, 0},
				new int[] {2, 0}};
		testPredicates(null, filter, expectedCounts);
	}
	
	@Test
	public void testStatefulFilter() {
		MetricsPredicate filter = MetricsValve.of(event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX));
		int[][] expectedCounts = {
				new int[] {0, 0},
				new int[] {1, 0},
				new int[] {2, 0},
				new int[] {3, 0}};
		testPredicates(null, filter, expectedCounts);
	}
	
	private void testPredicates(MetricsPredicate gate, MetricsPredicate filter, int[][] expectedCounts) {
		this.queue = MetricsTrail.hook(MetricsTrailConsumer.from("testConsumer", this.consumer, gate, filter));

		// USER A CAUSES 1 STANDARD METRIC EVENT
		MetricsTrail.commit(new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[0][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[0][1], this.queue.getGatedCount());

		// USER A CAUSES 1 IMPORTANT METRIC EVENT
		MetricsTrail.commit(new Metric(TEST_IMPORTANT_EVENT_PREFIX+"B", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[1][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[1][1], this.queue.getGatedCount());
		
		// USER A CAUSES 1 STANDARD METRIC EVENT
		MetricsTrail.commit(new Metric(TEST_EVENT_PREFIX+"C", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[2][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[2][1], this.queue.getGatedCount());

		// USER A CAUSES 1 IMPORTANT METRIC EVENT
		MetricsTrail.commit(new Metric(TEST_IMPORTANT_EVENT_PREFIX+"D", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[3][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[3][1], this.queue.getGatedCount());
	}
	
	private void waitUntilConsumed() {
		while (this.queue.getDeliveringCount() > 0) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to wait for event consuming");
			}
		}
	}
}
