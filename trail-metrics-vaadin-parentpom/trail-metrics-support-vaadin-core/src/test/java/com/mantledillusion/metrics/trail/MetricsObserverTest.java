package com.mantledillusion.metrics.trail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class MetricsObserverTest {

	private static final String SESSION_ID = "abc";
	private static final String SESSION_ID_ALT = "abcd";

	private static final String TEST_EVENT_PREFIX = "test.";
	
	private MockEnvironment env;
	private MockVaadinMetricsTrailSupport support;
	private MockMetricsConsumer consumer;
	
	@BeforeEach
	public void init() {
		this.env = new MockEnvironment();
		this.support = new MockVaadinMetricsTrailSupport(this.env);
		this.consumer = new MockMetricsConsumer();
	}
	
	@Test
	public void testBasicVisit() {
		this.support.hook(MetricsTrailConsumer.from("testConsumer", this.consumer));
		
		// USER A VISITS
		UUID correlationId = this.env.mockUserVisit(SESSION_ID);
		
		waitUntilConsumed();
		assertEquals(2, this.consumer.size(correlationId));
		
		Metric e1 = this.consumer.dequeueOne(correlationId);
		assertEquals(GeneralVaadinMetrics.SESSION_BEGIN.getMetricId(), e1.getIdentifier());

		e1 = this.consumer.dequeueOne(correlationId);
		assertEquals(GeneralVaadinMetrics.BROWSER_INFO.getMetricId(), e1.getIdentifier());

		// USER B VISITS
		UUID correlationIdAlt = this.env.mockUserVisit(SESSION_ID_ALT);
		
		waitUntilConsumed();
		assertEquals(0, this.consumer.size(correlationId));
		assertEquals(2, this.consumer.size(correlationIdAlt));
		
		Metric e2 = this.consumer.dequeueOne(correlationIdAlt);
		assertEquals(GeneralVaadinMetrics.SESSION_BEGIN.getMetricId(), e2.getIdentifier());

		e2 = this.consumer.dequeueOne(correlationIdAlt);
		assertEquals(GeneralVaadinMetrics.BROWSER_INFO.getMetricId(), e2.getIdentifier());
		
		// USER B CAUSES 1 METRIC
		Metric metricA = new Metric(TEST_EVENT_PREFIX+"A", MetricType.ALERT);
		this.env.mockDispatch(metricA);

		waitUntilConsumed();
		assertEquals(1, this.consumer.size(correlationIdAlt));

		Metric e3 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(metricA, e3);
		
		// USER B CAUSES 2 METRICS
		Metric metricB = new Metric(TEST_EVENT_PREFIX+"B", MetricType.ALERT);
		this.env.mockDispatch(metricB);
		Metric metricC = new Metric(TEST_EVENT_PREFIX+"C", MetricType.ALERT);
		this.env.mockDispatch(metricC);

		waitUntilConsumed();
		assertEquals(2, this.consumer.size(correlationIdAlt));

		Metric e4 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(metricB, e4);
		Metric e5 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(metricC, e5);
	}
	
	private void waitUntilConsumed() {
		while (this.support.getSessionTrail().isDelivering()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to wait for event consuming");
			}
		}
	}
}
