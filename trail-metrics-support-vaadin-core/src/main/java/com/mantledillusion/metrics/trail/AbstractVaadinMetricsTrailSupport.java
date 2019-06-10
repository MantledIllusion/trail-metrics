package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

abstract class AbstractVaadinMetricsTrailSupport<ServiceType, SessionType> {

    public static final String ATTRIBUTE_KEY_SESSION_ID = "sessionId";

    private final Set<MetricsTrailConsumer> consumers = Collections.newSetFromMap(new IdentityHashMap<>());

    protected AbstractVaadinMetricsTrailSupport(ServiceType service) {
        if (service == null) {
            throw new IllegalArgumentException("Cannot support a null service");
        }

        // SESSION INIT
        Consumer<SessionType> sessionInitListener = session -> {
            MetricsTrail trail = new MetricsTrail(UUID.randomUUID());
            AbstractVaadinMetricsTrailSupport.this.consumers.forEach(consumer -> trail.hook(consumer));
            hookSession(session, trail);

            trail.commit(GeneralVaadinMetrics.SESSION_BEGIN.build(
                    new MetricAttribute(ATTRIBUTE_KEY_SESSION_ID, getSessionId(session))));

            BrowserInfo browserInfo = getSessionBrowserInfo(session);
            trail.commit(GeneralVaadinMetrics.BROWSER_INFO.build(
                    new MetricAttribute(BrowserInfo.ATTRIBUTE_KEY_APPLICATION, browserInfo.getApplication()),
                    new MetricAttribute(BrowserInfo.ATTRIBUTE_KEY_TYPE, browserInfo.getBrowser().name()),
                    new MetricAttribute(BrowserInfo.ATTRIBUTE_KEY_VERSION, browserInfo.getVersion()),
                    new MetricAttribute(BrowserInfo.ATTRIBUTE_KEY_ENVIRONMENT, browserInfo.getEnvironment().name())));
        };

        // NAVIGATION
        BiConsumer<String, String> urlListener = new BiConsumer<String, String>() {

            @Override
            public void accept(String path, String query) {
                Metric metric = GeneralVaadinMetrics.NAVIGATION.build(path);
                for (Map.Entry<String, String> param : fromParamAppender(query).entrySet()) {
                    metric.getAttributes().add(new MetricAttribute(param.getKey(), param.getValue()));
                }
                getSessionTrail().commit(metric);
            }

            private final Map<String, String> fromParamAppender(String query) {
                Map<String, String> params = new HashMap<>();
                for (String param : query.split("&")) {
                    String[] splitted = param.split("=");
                    params.put(splitted[0], splitted[1]);
                }
                return params;
            }
        };

        // SESSION DESTROY
        Consumer<SessionType> sessionDestroyListener = session -> {
            getSessionTrail().commit(GeneralVaadinMetrics.SESSION_END.build());
            getSessionTrail().end();
        };

        observe(service, sessionInitListener, urlListener, sessionDestroyListener);
    }

    protected abstract void observe(ServiceType service, Consumer<SessionType> sessionInitListener,
                                    BiConsumer<String, String> urlListener,
                                    Consumer<SessionType> sessionDestroyListener);

    protected abstract void hookSession(SessionType session, MetricsTrail trail);

    protected abstract String getSessionId(SessionType session);

    protected abstract BrowserInfo getSessionBrowserInfo(SessionType session);

    protected abstract MetricsTrail getSessionTrail();

    /**
     * Hooks a {@link MetricsTrailConsumer} to this support to enable it retrieving metrics from all sessions.
     * <p>
     * Effectively, this will instruct this support to hook the given consumer to the {@link MetricsTrail}s created for
     * every new session of the Vaadin service this support supports.
     *
     * @see MetricsTrail#hook(MetricsTrailConsumer)
     * @param consumer The {@link MetricsTrailConsumer} to hook, might <b>not</b> be null.
     */
    public void hook(MetricsTrailConsumer consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("Cannot hook a null consumer");
        }
        this.consumers.add(consumer);
    }
}
