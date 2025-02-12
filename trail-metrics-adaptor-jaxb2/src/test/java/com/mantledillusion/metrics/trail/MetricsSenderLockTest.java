package com.mantledillusion.metrics.trail;

import java.util.ArrayList;
import java.util.List;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.web.WebMetricRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsSenderLockTest implements TestConstants {
	
	private static final int RETRY_AMOUNT = 3;

	private boolean causeError = true;
	private int receivedTries = 0;
	
	private boolean causeError() {
		return this.causeError;
	}
	
	private void receivedTry() {
		this.receivedTries++;
	}
	
	@Test
	public void testLock() throws Exception {
		List<WebMetricRequest> receivedRequests = new ArrayList<>();
		
		MetricsSender sender = MetricsSender.wrap(request -> {
			receivedTry();
			if (causeError()) {
				throw new RuntimeException();
			}
			receivedRequests.add(request);
		});
		sender.setMode(MetricsSender.SenderMode.PACKAGED);
		sender.setMaxRetryCount(RETRY_AMOUNT);
		sender.setSendingRetryIntervals(10);

		// TEST PACKAGE METRIC BUT UNABLE TO SEND
		Event event = new Event(IDENTIFIER);
		
		consume(sender, CONSUMER, TRAIL_ID, event);
		
		assertEquals(0, receivedRequests.size());
		assertTrue(sender.isLocked());
		assertEquals(RETRY_AMOUNT+1, this.receivedTries);
		
		// TEST CONSUME ANOTHER METRIC ALTHOUGH SENDER IS LOCKED
		Exception e = null;
		try {
			consume(sender, CONSUMER, TRAIL_ID, event);
		} catch (Exception ex)  {
			e = ex;
		}
		assertNotNull(e);
		assertTrue(e instanceof IllegalStateException);
		assertEquals(RETRY_AMOUNT+1, this.receivedTries);
		
		// TEST UNLOCK SENDER CAUSES RESENDING
		this.causeError = false;
		sender.unlock();
		consume(sender);
		
		assertEquals(1, receivedRequests.size());
		assertFalse(sender.isLocked());
		assertEquals(RETRY_AMOUNT+2, this.receivedTries);
		
		// TEST CONSUME ANOTHER METRIC IN UNLOCKED SENDER
		consume(sender, CONSUMER, TRAIL_ID, event);
		
		assertEquals(2, receivedRequests.size());
		assertEquals(RETRY_AMOUNT+3, this.receivedTries);
	}
}
