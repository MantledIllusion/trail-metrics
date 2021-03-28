package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class MockMetricsConsumer implements MetricsConsumer {

	private final Map<UUID, Queue<Event>> queues = new HashMap<>();

	int size(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).size();
	}
	
	Event dequeueOne(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).remove();
	}

	@Override
	public void consume(String consumerId, UUID correlationId, Event event) throws Exception {
		this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).add(event);
	}
}
