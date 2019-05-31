package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class MockMetricsConsumer implements MetricsConsumer {

	private final Map<UUID, Queue<Metric>> queues = new HashMap<>();
	
	@Override
	public void consume(String consumerId, UUID trailId, Metric metric) throws Exception {
		this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).add(metric);
	}

	int size(UUID trailId) {
		return this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).size();
	}
	
	Metric dequeueOne(UUID trailId) {
		return this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).remove();
	}
}
