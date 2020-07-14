package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

abstract class AbstractTrailMetricsHttpServerHandler {

    private static final ThreadLocal<Long> REQUEST_DURATION = new ThreadLocal<>();

    private static final String MID_REQUEST = "spring.web.server.request";
    private static final String AKEY_ENDPOINT = "endpoint";
    private static final String AKEY_ORIGINAL_CORRELATION_ID = "originalCorrelationId";

    private static final String MID_RESPONSE = "spring.web.server.response";

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.correlationIdHeaderName";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.http.incomingMode";
    public static final String PRTY_FOLLOW_SESSIONS = "trailMetrics.http.server.followSessions";
    public static final String PRTY_DISPATCH_REQUEST = "trailMetrics.http.dispatchRequest";
    public static final String PRTY_DISPATCH_RESPONSE = "trailMetrics.http.dispatchResponse";
    public static final String DEFAULT_HEADER_NAME = "correlationId";
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";
    public static final boolean DEFAULT_FOLLOW_SESSIONS = true;
    public static final boolean DEFAULT_DISPATCH_REQUEST = false;
    public static final boolean DEFAULT_DISPATCH_RESPONSE = false;

    private final String headerName;
    private TrailBehaviourMode incomingMode;
    private boolean followSessions;
    private boolean dispatchRequest;
    private boolean dispatchResponse;

    public AbstractTrailMetricsHttpServerHandler(String headerName, TrailBehaviourMode incomingMode, boolean followSessions,
                                                 boolean dispatchRequest, boolean dispatchResponse) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
        setIncomingMode(incomingMode);
        this.followSessions = followSessions;
        this.dispatchRequest = dispatchRequest;
        this.dispatchResponse = dispatchResponse;
    }

    /**
     * Returns the currently used mode for incoming headers.
     *
     * @return The current mode, never null
     */
    public TrailBehaviourMode getIncomingMode() {
        return this.incomingMode;
    }

    /**
     * Sets the used mode for incoming headers.
     *
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     */
    public void setIncomingMode(TrailBehaviourMode incomingMode) {
        if (incomingMode == null) {
            throw new IllegalArgumentException("Cannot set a null mode");
        }
        this.incomingMode = incomingMode;
    }

    public boolean isFollowSessions() {
        return followSessions;
    }

    public void setFollowSessions(boolean followSessions) {
        this.followSessions = followSessions;
    }

    /**
     * Returns whether to dispatch a metric when a request comes in.
     *
     * @return True if a metric is dispatched, false otherwise.
     */
    public boolean isDispatchRequest() {
        return this.dispatchRequest;
    }

    /**
     * Sets whether to dispatch a metric when a request comes in.
     *
     * @param dispatchRequest True if a metric should be dispatched, false otherwise.
     */
    public void setDispatchRequest(boolean dispatchRequest) {
        this.dispatchRequest = dispatchRequest;
    }

    /**
     * Returns whether to dispatch a metric when a request is responded to.
     *
     * @return True if a metric is dispatched, false otherwise.
     */
    public boolean isDispatchResponse() {
        return dispatchResponse;
    }

    /**
     * Sets whether to dispatch a metric when a request is responded to.
     *
     * @param dispatchResponse True if a metric should be dispatched, false otherwise.
     */
    public void setDispatchResponse(boolean dispatchResponse) {
        this.dispatchResponse = dispatchResponse;
    }

    protected boolean requestStart(ServletRequest request) {
        try {
            if (request instanceof HttpServletRequest) {
                MetricsTrailSupport.begin(UUID.fromString(((HttpServletRequest) request).getHeader(this.headerName)));
            } else {
                MetricsTrailSupport.begin();
            }
            dispatchRequestMetric(request);
        } catch (NullPointerException | IllegalArgumentException e) {
            switch (this.incomingMode) {
                case STRICT:
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
                case LENIENT:
                    if (this.followSessions && request instanceof HttpServletRequest) {
                        try {
                            MetricsTrailSupport.begin(UUID.fromString((String) ((HttpServletRequest) request).
                                    getSession().getAttribute(this.headerName)));
                        } catch (NullPointerException | IllegalArgumentException e2) {
                            MetricsTrailSupport.begin();
                            ((HttpServletRequest) request).getSession().setAttribute(this.headerName,
                                    MetricsTrailSupport.get().getCorrelationId().toString());
                        }
                    } else {
                        MetricsTrailSupport.begin();
                    }
                    dispatchRequestMetric(request);
            }
        }

        REQUEST_DURATION.set(System.currentTimeMillis());
        return true;
    }

    private void dispatchRequestMetric(ServletRequest request) {
        if (this.dispatchRequest) {
            Metric metric = new Metric(MID_REQUEST, MetricType.ALERT);
            if (request instanceof HttpServletRequest) {
                metric.getAttributes().add(new MetricAttribute(AKEY_ENDPOINT, ((HttpServletRequest) request).getRequestURI()));
                if (((HttpServletRequest) request).getHeader(this.headerName) != null) {
                    metric.getAttributes().add(new MetricAttribute(AKEY_ORIGINAL_CORRELATION_ID,
                            ((HttpServletRequest) request).getHeader(this.headerName)));
                }
            }
            MetricsTrailSupport.commit(metric, false);
        }
    }

    protected void requestEnd(ServletResponse response) {
        if (this.dispatchResponse) {
            MetricsTrailSupport.commit(new Metric(MID_RESPONSE, MetricType.METER, System.currentTimeMillis()-REQUEST_DURATION.get()), false);
        }
        REQUEST_DURATION.set(null);

        if (MetricsTrailSupport.has()) {
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).addHeader(this.headerName, MetricsTrailSupport.id().toString());
            }
            MetricsTrailSupport.end();
        }
    }
}
