package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.web.WebMetric;

import java.util.UUID;

public interface TestConstants {
	
	String IDENTIFIER = "some.metric.id";
	String CONSUMER = "abc";
	UUID TRAIL_ID = UUID.randomUUID();
	
	default void consume(MetricsSender sender) throws Exception {
		consume(sender, null, null);
	}
	
	default void consume(MetricsSender sender, String consumerId, UUID correlationId, Event... events) throws Exception {
		for (Event event : events) {
			sender.consume(consumerId, correlationId, event);
		}
		
		while (true) {
			Thread.sleep(100);
			if (!sender.isSendingPackage() && sender.getAwaitingCount()==0) {
				return;
			}
		}
	}

	default boolean equals(Event o1, WebMetric o2) {
		return o1.getIdentifier().equals(o2.getIdentifier()) &&
				o1.getTimestamp().equals(o2.getTimestamp());
	}
}
