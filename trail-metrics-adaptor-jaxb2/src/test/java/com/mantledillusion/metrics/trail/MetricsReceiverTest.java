package com.mantledillusion.metrics.trail;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.web.*;
import org.junit.jupiter.api.Test;

public class MetricsReceiverTest implements TestConstants {

	@Test
	public void testSend() throws InterruptedException, ExecutionException {
		// SETUP
		Set<Event> aEvents = Collections.newSetFromMap(new IdentityHashMap<>());
		CompletableFuture<Set<Event>> aMetricsCompleteable = new CompletableFuture<>();
		
		MetricsConsumer aConsumer = (consumerId, sessionId, metric) -> {
			aEvents.add(metric);
			if (aEvents.size() == 2) {
				aMetricsCompleteable.complete(aEvents);
			}
		};
		
		Set<Event> bEvents = Collections.newSetFromMap(new IdentityHashMap<>());
		CompletableFuture<Set<Event>> bMetricsCompleteable = new CompletableFuture<>();
		
		MetricsConsumer bConsumer = (consumerId, sessionId, metric) -> {
			bEvents.add(metric);
			if (bEvents.size() == 2) {
				bMetricsCompleteable.complete(bEvents);
			}
		};
		
		MetricsReceiver receiver = new MetricsReceiver();
		receiver.addConsumer("consumer\\.a\\..*", "a", aConsumer);
		receiver.addConsumer("consumer\\.b\\..*", "b", bConsumer);
		
		// CREATE REQUEST
		WebMetricRequest request = new WebMetricRequest();
		
		WebMetricConsumer consumerA = new WebMetricConsumer("consumer.a.test");
		request.getConsumers().add(consumerA);
		
		WebMetricTrail trailA = new WebMetricTrail(UUID.randomUUID().toString());
		consumerA.getTrails().add(trailA);
		
		WebMetric metricA1 = new WebMetric("a1");
		trailA.getMetrics().add(metricA1);

		WebMetric metricA2 = new WebMetric("a2");
		trailA.getMetrics().add(metricA2);
		
		WebMetricConsumer consumerB = new WebMetricConsumer("consumer.b.test");
		request.getConsumers().add(consumerB);
		
		WebMetricTrail trailB = new WebMetricTrail(UUID.randomUUID().toString());
		consumerB.getTrails().add(trailB);

		WebMetric metricB1 = new WebMetric("b1");
		trailB.getMetrics().add(metricB1);

		WebMetric metricB2 = new WebMetric("b2");
		trailB.getMetrics().add(metricB2);
		
		// TEST RECEIVE
		
		receiver.receive(request);
		
		aMetricsCompleteable.get();
		contains(aEvents, metricA1);
		contains(aEvents, metricA2);
		
		bMetricsCompleteable.get();
		contains(bEvents, metricB1);
		contains(bEvents, metricB2);
	}

	private void contains(Set<Event> events, WebMetric metric) {
		for (Event e: events) {
			if (equals(e, metric)) {
				return;
			}
		}
		fail();
	}
}
