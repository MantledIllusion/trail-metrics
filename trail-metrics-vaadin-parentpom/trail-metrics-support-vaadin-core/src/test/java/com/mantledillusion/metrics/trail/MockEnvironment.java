package com.mantledillusion.metrics.trail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.mantledillusion.metrics.trail.MockService.MockRequest;
import com.mantledillusion.metrics.trail.MockService.MockSession;
import com.mantledillusion.metrics.trail.api.Metric;

public class MockEnvironment {

	private final List<MockSession> sessions = new ArrayList<>();
	
	private Consumer<MockSession> sessionListener;
	
	public MockSession getCurrentSession() {
		return sessions.get(sessions.size()-1);
	}
	
	public MockRequest getCurrentRequest() {
		return getCurrentSession().getCurrentRequest();
	}

	public void setSessionListener(Consumer<MockSession> sessionListener) {
		this.sessionListener = sessionListener;
	}
	
	public UUID mockUserVisit(String sessionId) {
		MockSession session = new MockSession(sessionId);
		this.sessions.add(session);
		this.sessionListener.accept(session);
		return ((MetricsTrail) session.get(MetricsTrail.class)).getCorrelationId();
	}
	
	public void mockDispatch(Metric metric) {
		getCurrentSession().addRequest(new MockRequest());
		((MetricsTrail) getCurrentSession().get(MetricsTrail.class)).commit(metric);
	}
}
