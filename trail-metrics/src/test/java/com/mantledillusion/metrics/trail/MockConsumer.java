package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

import java.util.*;
import java.util.concurrent.*;

public class MockConsumer implements MetricsConsumer {

	private final Semaphore semaphore = new Semaphore(Integer.MAX_VALUE);
	private final Map<UUID, Queue<Event>> queues = new HashMap<>();
	private final Map<UUID, Integer> fails = new HashMap<>();
	private int failLevel = 0;
	
	@Override
	public void consume(String consumerId, UUID correlationId, Event event) throws Exception {
		this.semaphore.acquire();
		if (this.failLevel == 2) {
			this.fails.put(correlationId, this.fails.computeIfAbsent(correlationId, id -> Integer.valueOf(0))+1);
			throw new Error();
		} else if (this.failLevel == 1) {
			this.fails.put(correlationId, this.fails.computeIfAbsent(correlationId, id -> Integer.valueOf(0))+1);
			throw new IllegalStateException();
		} else {
			this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).add(event);
		}
	}

	int size(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).size();
	}

	int fails(UUID correlationId) {
		return this.fails.computeIfAbsent(correlationId, id -> Integer.valueOf(0));
	}

	void block() {
		this.semaphore.drainPermits();
	}

	void unblockAll() {
		this.semaphore.release(Integer.MAX_VALUE);
	}

	void unblockOne() {
		this.semaphore.release();
	}

	void breakSystem() {
		this.failLevel = 2;
	}

	void breakConsumer() {
		this.failLevel = 1;
	}

	void healConsumer() {
		this.failLevel = 0;
	}
	
	Event dequeueOne(UUID correlationId) {
		return this.queues.computeIfAbsent(correlationId, id -> new LinkedBlockingQueue<>()).remove();
	}
}
