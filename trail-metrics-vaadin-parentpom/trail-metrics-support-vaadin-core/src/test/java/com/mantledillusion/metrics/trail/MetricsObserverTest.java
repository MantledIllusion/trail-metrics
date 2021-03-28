package com.mantledillusion.metrics.trail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.mantledillusion.metrics.trail.api.Event;
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
		
		Event e1 = this.consumer.dequeueOne(correlationId);
		assertEquals(GeneralVaadinMetrics.SESSION_BEGIN.getIdentifier(), e1.getIdentifier());

		e1 = this.consumer.dequeueOne(correlationId);
		assertEquals(GeneralVaadinMetrics.BROWSER_INFO.getIdentifier(), e1.getIdentifier());

		// USER B VISITS
		UUID correlationIdAlt = this.env.mockUserVisit(SESSION_ID_ALT);
		
		waitUntilConsumed();
		assertEquals(0, this.consumer.size(correlationId));
		assertEquals(2, this.consumer.size(correlationIdAlt));
		
		Event e2 = this.consumer.dequeueOne(correlationIdAlt);
		assertEquals(GeneralVaadinMetrics.SESSION_BEGIN.getIdentifier(), e2.getIdentifier());

		e2 = this.consumer.dequeueOne(correlationIdAlt);
		assertEquals(GeneralVaadinMetrics.BROWSER_INFO.getIdentifier(), e2.getIdentifier());
		
		// USER B CAUSES 1 METRIC
		Event eventA = new Event(TEST_EVENT_PREFIX+"A");
		this.env.mockDispatch(eventA);

		waitUntilConsumed();
		assertEquals(1, this.consumer.size(correlationIdAlt));

		Event e3 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(eventA, e3);
		
		// USER B CAUSES 2 METRICS
		Event eventB = new Event(TEST_EVENT_PREFIX+"B");
		this.env.mockDispatch(eventB);
		Event eventC = new Event(TEST_EVENT_PREFIX+"C");
		this.env.mockDispatch(eventC);

		waitUntilConsumed();
		assertEquals(2, this.consumer.size(correlationIdAlt));

		Event e4 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(eventB, e4);
		Event e5 = this.consumer.dequeueOne(correlationIdAlt);
		assertSame(eventC, e5);
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
