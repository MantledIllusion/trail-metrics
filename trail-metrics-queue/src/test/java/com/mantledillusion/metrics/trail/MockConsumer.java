package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;

import java.util.*;
import java.util.concurrent.*;

public class MockConsumer implements MetricsConsumer {

	private final Semaphore semaphore = new Semaphore(Integer.MAX_VALUE);
	private final Map<UUID, Queue<Metric>> queues = new HashMap<>();
	private final Map<UUID, Integer> fails = new HashMap<>();
	private int failLevel = 0;
	
	@Override
	public void consume(String consumerId, UUID trailId, Metric metric) throws Exception {
		this.semaphore.acquire();
		if (this.failLevel == 2) {
			this.fails.put(trailId, this.fails.computeIfAbsent(trailId, id -> Integer.valueOf(0))+1);
			throw new Error();
		} else if (this.failLevel == 1) {
			this.fails.put(trailId, this.fails.computeIfAbsent(trailId, id -> Integer.valueOf(0))+1);
			throw new IllegalStateException();
		} else {
			this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).add(metric);
		}
	}

	int size(UUID trailId) {
		return this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).size();
	}

	int fails(UUID trailId) {
		return this.fails.computeIfAbsent(trailId, id -> Integer.valueOf(0));
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
	
	Metric dequeueOne(UUID trailId) {
		return this.queues.computeIfAbsent(trailId, id -> new LinkedBlockingQueue<>()).remove();
	}
}
