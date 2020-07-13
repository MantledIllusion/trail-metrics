package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * {@link HandlerInterceptor} that will begin a {@link MetricsTrail} on the {@link Thread} of an incoming HTTP request
 * and will end it when the request is responded to.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, TrailBehaviourMode, boolean, boolean)}
 * or the {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, String, boolean, boolean)} or the
 * {@value #PRTY_INCOMING_MODE} property to set the mode to use, which is {@value #DEFAULT_INCOMING_MODE} by default.
 * <p>
 * The incoming HTTP request header is according to the {@link TrailBehaviourMode} set. When returning the response,
 * the HTTP response header is set to whatever ID was used to identify the {@link MetricsTrail}.
 */
public class TrailMetricsHttpServerInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> REQUEST_DURATION = new ThreadLocal<>();

    private static final String MID_REQUEST = "spring.web.server.request";
    private static final String AKEY_ENDPOINT = "endpoint";
    private static final String AKEY_ORIGINAL_CORRELATION_ID = "originalCorrelationId";

    private static final String MID_RESPONSE = "spring.web.server.response";

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.correlationIdHeaderName";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.http.incomingMode";
    public static final String PRTY_DISPATCH_REQUEST = "trailMetrics.http.dispatchRequest";
    public static final String PRTY_DISPATCH_RESPONSE = "trailMetrics.http.dispatchResponse";
    public static final String DEFAULT_HEADER_NAME = "correlationId";
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";
    public static final boolean DEFAULT_DISPATCH_REQUEST = true;
    public static final boolean DEFAULT_DISPATCH_RESPONSE = true;

    private final String headerName;
    private TrailBehaviourMode incomingMode;
    private boolean dispatchRequest;
    private boolean dispatchResponse;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsHttpServerInterceptor() {
        this(DEFAULT_HEADER_NAME, TrailBehaviourMode.LENIENT, true, true);
    }

    /**
     * Advanced constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    public TrailMetricsHttpServerInterceptor(String headerName) {
        this(headerName, TrailBehaviourMode.LENIENT, true, true);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param dispatchRequest Whether or not to dispatch an event when a request begins.
     * @param dispatchResponse Whether or not to dispatch an event when a request is responded to.
     */
    @Autowired
    public TrailMetricsHttpServerInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                             @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode,
                                             @Value("${"+ PRTY_DISPATCH_REQUEST +":"+ DEFAULT_DISPATCH_REQUEST +"}") boolean dispatchRequest,
                                             @Value("${"+ PRTY_DISPATCH_RESPONSE +":"+ DEFAULT_DISPATCH_RESPONSE +"}") boolean dispatchResponse) {
        this(headerName, TrailBehaviourMode.valueOf(incomingMode), dispatchRequest, dispatchResponse);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param dispatchRequest Whether or not to dispatch an event when a request begins.
     * @param dispatchResponse Whether or not to dispatch an event when a request is responded to.
     */
    public TrailMetricsHttpServerInterceptor(String headerName, TrailBehaviourMode incomingMode,
                                             boolean dispatchRequest, boolean dispatchResponse) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
        setIncomingMode(incomingMode);
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            MetricsTrailSupport.begin(UUID.fromString(request.getHeader(this.headerName)));
            dispatchRequestMetric(request, null);
        } catch (NullPointerException | IllegalArgumentException e) {
            switch (this.incomingMode) {
                case STRICT:
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
                case LENIENT:
                    MetricsTrailSupport.begin();
                    dispatchRequestMetric(request, request.getHeader(this.headerName));
            }
        }
        if (MetricsTrailSupport.has()) {
            response.addHeader(this.headerName, MetricsTrailSupport.id().toString());
        }
        return true;
    }

    private void dispatchRequestMetric(HttpServletRequest request, String originalCorrelationId) {
        if (this.dispatchRequest) {
            Metric metric = new Metric(MID_REQUEST, MetricType.ALERT);
            metric.getAttributes().add(new MetricAttribute(AKEY_ENDPOINT, request.getRequestURI()));
            if (originalCorrelationId != null) {
                metric.getAttributes().add(new MetricAttribute(AKEY_ORIGINAL_CORRELATION_ID, originalCorrelationId));
            }
            MetricsTrailSupport.commit(metric);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }

        if (this.dispatchResponse) {
            MetricsTrailSupport.commit(new Metric(MID_RESPONSE, MetricType.METER, REQUEST_DURATION.get()));
        }
    }
}
