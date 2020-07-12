package com.mantledillusion.metrics.trail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.web.WebMetricConsumer;
import com.mantledillusion.metrics.trail.api.web.WebMetricRequest;
import com.mantledillusion.metrics.trail.api.web.WebMetricTrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsSenderTransferTest implements TestConstants {
	
	private final List<WebMetricRequest> receivedRequests = new ArrayList<>();
	private MetricsSender sender;
	
	@BeforeEach
	public void before() {
		this.sender = MetricsSender.wrap(request -> this.receivedRequests.add(request));
	}
	
	@Test
	public void testMetricRequestMapping() throws Exception {
		Metric metric = new Metric(IDENTIFIER, MetricType.ALERT);
		
		consume(this.sender, CONSUMER, TRAIL_ID, metric);
		
		assertEquals(1, this.receivedRequests.size());
		WebMetricRequest request = this.receivedRequests.get(0);

		assertEquals(1, request.getConsumers().size());
		WebMetricConsumer consumer = request.getConsumers().get(0);
		assertEquals(CONSUMER, consumer.getConsumerId());
		
		assertEquals(1, consumer.getTrails().size());
		WebMetricTrail trail = consumer.getTrails().get(0);
		assertEquals(TRAIL_ID, UUID.fromString(trail.getCorrelationId()));

		assertEquals(1, trail.getMetrics().size());
		assertTrue(equals(metric, trail.getMetrics().get(0)));
	}
	
	@Test
	public void testSendSynchronously() throws Exception {
		this.sender.setMode(MetricsSender.SenderMode.SYNCHRONOUS);
		
		Metric metric = new Metric(IDENTIFIER, MetricType.ALERT);
		
		consume(this.sender, CONSUMER, TRAIL_ID, metric, metric);
		
		assertEquals(2, this.receivedRequests.size());
	}
	
	@Test
	public void testSendPackaged() throws Exception {
		this.sender.setMode(MetricsSender.SenderMode.PACKAGED);
		
		Metric metric = new Metric(IDENTIFIER, MetricType.ALERT);
		
		consume(this.sender, CONSUMER, TRAIL_ID, metric, metric);
		
		assertEquals(1, this.receivedRequests.size());
		WebMetricRequest request = this.receivedRequests.get(0);

		assertEquals(1, request.getConsumers().size());
		WebMetricConsumer consumer = request.getConsumers().get(0);
		assertEquals(CONSUMER, consumer.getConsumerId());
		
		assertEquals(1, consumer.getTrails().size());
		WebMetricTrail trail = consumer.getTrails().get(0);
		assertEquals(TRAIL_ID, UUID.fromString(trail.getCorrelationId()));

		assertEquals(2, trail.getMetrics().size());
	}
}