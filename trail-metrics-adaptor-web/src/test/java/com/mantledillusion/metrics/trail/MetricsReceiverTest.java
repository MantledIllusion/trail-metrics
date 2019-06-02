package com.mantledillusion.metrics.trail;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.web.*;
import org.junit.jupiter.api.Test;


public class MetricsReceiverTest implements TestConstants {

	@Test
	public void testSend() throws InterruptedException, ExecutionException {
		// SETUP
		Set<Metric> aMetrics = Collections.newSetFromMap(new IdentityHashMap<>());
		CompletableFuture<Set<Metric>> aMetricsCompleteable = new CompletableFuture<>();
		
		MetricsConsumer aConsumer = (consumerId, sessionId, metric) -> {
			aMetrics.add(metric);
			if (aMetrics.size() == 2) {
				aMetricsCompleteable.complete(aMetrics);
			}
		};
		
		Set<Metric> bMetrics = Collections.newSetFromMap(new IdentityHashMap<>());
		CompletableFuture<Set<Metric>> bMetricsCompleteable = new CompletableFuture<>();
		
		MetricsConsumer bConsumer = (consumerId, sessionId, metric) -> {
			bMetrics.add(metric);
			if (bMetrics.size() == 2) {
				bMetricsCompleteable.complete(bMetrics);
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
		
		WebMetric metricA1 = new WebMetric("a1", WebMetricType.ALERT);
		trailA.getMetrics().add(metricA1);

		WebMetric metricA2 = new WebMetric("a2", WebMetricType.ALERT);
		trailA.getMetrics().add(metricA2);
		
		WebMetricConsumer consumerB = new WebMetricConsumer("consumer.b.test");
		request.getConsumers().add(consumerB);
		
		WebMetricTrail trailB = new WebMetricTrail(UUID.randomUUID().toString());
		consumerB.getTrails().add(trailB);

		WebMetric metricB1 = new WebMetric("b1", WebMetricType.ALERT);
		trailB.getMetrics().add(metricB1);

		WebMetric metricB2 = new WebMetric("b2", WebMetricType.ALERT);
		trailB.getMetrics().add(metricB2);
		
		// TEST RECEIVE
		
		receiver.receive(request);
		
		aMetricsCompleteable.get();
		contains(aMetrics, metricA1);
		contains(aMetrics, metricA2);
		
		bMetricsCompleteable.get();
		contains(bMetrics, metricB1);
		contains(bMetrics, metricB2);
	}

	private void contains(Set<Metric> metrics, WebMetric metric) {
		for (Metric e: metrics) {
			if (equals(e, metric)) {
				return;
			}
		}
		fail();
	}
}
