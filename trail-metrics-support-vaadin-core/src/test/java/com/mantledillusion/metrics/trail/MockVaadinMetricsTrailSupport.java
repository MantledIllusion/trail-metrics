package com.mantledillusion.metrics.trail;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mantledillusion.metrics.trail.MockService.MockSession;

public class MockVaadinMetricsTrailSupport extends AbstractVaadinMetricsTrailSupport<MockService, MockSession> {

	private MockEnvironment env;
	private Consumer<MockSession> sessionInitListener;

	MockVaadinMetricsTrailSupport(MockEnvironment env) {
		super(new MockService());
		this.env = env;
		this.env.setSessionListener(this.sessionInitListener);
	}

	@Override
	protected void observe(MockService service, Consumer<MockSession> sessionInitListener,
						   BiConsumer<String, String> urlListener, Consumer<MockSession> sessionDestroyListener) {
		this.sessionInitListener = sessionInitListener;
	}

	@Override
	protected void hookSession(MockSession session, MetricsTrail trail) {
		session.put(MetricsTrail.class, trail);
	}

	@Override
	protected String getSessionId(MockSession session) {
		return session.getId();
	}

	@Override
	protected BrowserInfo getSessionBrowserInfo(MockSession session) {
		return new BrowserInfo("", BrowserInfo.BrowserType.UNKNOWN, "0.0", BrowserInfo.SystemEnvironmentType.UNKNOWN);
	}

	@Override
	protected MetricsTrail getSessionTrail() {
		return (MetricsTrail) this.env.getCurrentSession().get(MetricsTrail.class);
	}
}
