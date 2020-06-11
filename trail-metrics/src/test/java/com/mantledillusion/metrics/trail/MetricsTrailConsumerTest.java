package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsTrailConsumerTest extends AbstractMetricsTest {

	private MetricsTrail trail;

	@BeforeEach
	public void beginTrail() {
		this.trail = new MetricsTrail(TRAIL_ID);
	}
	
	@Test
	public void testStatelessGate() {
		MetricsPredicate gate = event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX);
		int[][] expectedCounts = {
				new int[] {0, 1},
				new int[] {2, 0},
				new int[] {2, 1},
				new int[] {4, 0}};
		testPredicates(gate, null, expectedCounts);
	}
	
	@Test
	public void testStatefulGate() {
		MetricsPredicate gate = MetricsValve.of(event -> event.getIdentifier().startsWith(TEST_IMPORTANT_EVENT_PREFIX));
		int[][] expectedCounts = {
				new int[] {0, 1},
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
		this.queue = this.trail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer, gate, filter));

		// USER A CAUSES 1 STANDARD METRIC EVENT
		this.trail.commit(new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[0][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[0][1] > 0, this.queue.hasGated());
		assertEquals(expectedCounts[0][1], this.queue.getGatedCount());

		// USER A CAUSES 1 IMPORTANT METRIC EVENT
		this.trail.commit(new Metric(TEST_IMPORTANT_EVENT_PREFIX+"B", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[1][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[1][1] > 0, this.queue.hasGated());
		assertEquals(expectedCounts[1][1], this.queue.getGatedCount());
		
		// USER A CAUSES 1 STANDARD METRIC EVENT
		this.trail.commit(new Metric(TEST_EVENT_PREFIX+"C", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[2][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[2][1] > 0, this.queue.hasGated());
		assertEquals(expectedCounts[2][1], this.queue.getGatedCount());

		// USER A CAUSES 1 IMPORTANT METRIC EVENT
		this.trail.commit(new Metric(TEST_IMPORTANT_EVENT_PREFIX+"D", MetricType.ALERT));
		
		waitUntilConsumed();
		assertEquals(expectedCounts[3][0], this.consumer.size(TRAIL_ID));
		assertEquals(expectedCounts[3][1] > 0, this.queue.hasGated());
		assertEquals(expectedCounts[3][1], this.queue.getGatedCount());
	}
}
