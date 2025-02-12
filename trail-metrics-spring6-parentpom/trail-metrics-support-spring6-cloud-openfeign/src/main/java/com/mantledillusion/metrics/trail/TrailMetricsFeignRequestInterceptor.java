package com.mantledillusion.metrics.trail;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * {@link RequestInterceptor} that will add the calling {@link Thread} {@link MetricsTrail}'s ID as a HTTP header to
 * any outgoing request.
 * <p>
 * Use {@link TrailMetricsFeignRequestInterceptor#TrailMetricsFeignRequestInterceptor(String, TrailBehaviourMode)}} or the
 * {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * Use {@link TrailMetricsFeignRequestInterceptor#TrailMetricsFeignRequestInterceptor(String, TrailBehaviourMode)}} or the
 * {@value #PRTY_OUTGOING_MODE} property to set the mode to use, which is {@value #DEFAULT_OUTGOING_MODE} by default.
 */
public class TrailMetricsFeignRequestInterceptor implements RequestInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.correlationIdHeaderName";
    public static final String PRTY_OUTGOING_MODE = "trailMetrics.http.outgoingMode";
    public static final String DEFAULT_HEADER_NAME = MetricsTrailSupport.DEFAULT_TRAIL_ID_KEY;
    public static final String DEFAULT_OUTGOING_MODE = "OPTIONAL";

    private final String headerName;
    private TrailBehaviourMode outgoingMode;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     * <p>
     * Sets the mode of outgoing headers to {@link TrailBehaviourMode#OPTIONAL}.
     */
    public TrailMetricsFeignRequestInterceptor() {
        this(DEFAULT_HEADER_NAME, TrailBehaviourMode.OPTIONAL);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    public TrailMetricsFeignRequestInterceptor(String headerName) {
        this(headerName, TrailBehaviourMode.OPTIONAL);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param outgoingMode The behaviour mode for outgoing headers; might <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsFeignRequestInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                               @Value("${"+PRTY_OUTGOING_MODE+":"+DEFAULT_OUTGOING_MODE+"}") String outgoingMode) {
        this(headerName, TrailBehaviourMode.valueOf(outgoingMode));
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param outgoingMode The behaviour mode for outgoing headers; might <b>not</b> be null.
     */
    public TrailMetricsFeignRequestInterceptor(String headerName, TrailBehaviourMode outgoingMode) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
        setOutgoingMode(outgoingMode);
    }

    /**
     * Returns the currently used mode for outgoing headers.
     *
     * @return The current mode, never null
     */
    public TrailBehaviourMode getOutgoingMode() {
        return this.outgoingMode;
    }

    /**
     * Sets the used mode for outgoing headers.
     *
     * @param outgoingMode The behaviour mode for outgoing headers; might <b>not</b> be null.
     */
    public void setOutgoingMode(TrailBehaviourMode outgoingMode) {
        if (outgoingMode == null) {
            throw new IllegalArgumentException("Cannot set a null mode");
        }
        this.outgoingMode = outgoingMode;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (MetricsTrailSupport.has()) {
            requestTemplate.header(this.headerName, MetricsTrailSupport.id().toString());
        } else {
            switch (this.outgoingMode) {
                case STRICT:
                    throw new IllegalStateException("Cannot send a request without a trail");
                case LENIENT:
                    requestTemplate.header(this.headerName, UUID.randomUUID().toString());
            }
        }
    }
}
