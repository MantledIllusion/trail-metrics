package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.web.WebMetric;

import java.util.UUID;

public interface TestConstants {
	
	String IDENTIFIER = "some.metric.id";
	String CONSUMER = "abc";
	UUID TRAIL_ID = UUID.randomUUID();
	
	default void consume(MetricsSender sender) throws Exception {
		consume(sender, null, null);
	}
	
	default void consume(MetricsSender sender, String consumerId, UUID correlationId, Metric... metrics) throws Exception {
		for (Metric metric: metrics) {
			sender.consume(consumerId, correlationId, metric);
		}
		
		while (true) {
			Thread.sleep(100);
			if (!sender.isSendingPackage() && sender.getAwaitingCount()==0) {
				return;
			}
		}
	}

	default boolean equals(Metric o1, WebMetric o2) {
		return o1.getIdentifier().equals(o2.getIdentifier()) &&
				o1.getType().equals(MetricType.valueOf(o2.getType().name())) &&
				o1.getTimestamp().equals(o2.getTimestamp());
	}
}
