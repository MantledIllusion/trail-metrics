package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class MockMetricsConsumer implements MetricsConsumer {

	private final Map<UUID, Queue<Metric>> queues = new HashMap<>();

	int size(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).size();
	}
	
	Metric dequeueOne(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).remove();
	}

	@Override
	public void consume(String consumerId, UUID correlationId, Metric metric) throws Exception {
		this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).add(metric);
	}
}
