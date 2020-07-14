package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerInterceptor} that will begin a {@link MetricsTrail} on the {@link Thread} of an incoming HTTP request
 * and will end it when the request is responded to.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, TrailBehaviourMode, boolean, boolean, boolean)}
 * or the {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, String, boolean, boolean, boolean)} or the
 * {@value #PRTY_INCOMING_MODE} property to set the mode to use, which is {@value #DEFAULT_INCOMING_MODE} by default.
 * <p>
 * The incoming HTTP request header is according to the {@link TrailBehaviourMode} set. When returning the response,
 * the HTTP response header is set to whatever ID was used to identify the {@link MetricsTrail}.
 */
public class TrailMetricsHttpServerInterceptor extends AbstractTrailMetricsHttpServerHandler implements HandlerInterceptor {


    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsHttpServerInterceptor() {
        super(DEFAULT_HEADER_NAME, TrailBehaviourMode.LENIENT, DEFAULT_FOLLOW_SESSIONS, DEFAULT_DISPATCH_REQUEST, DEFAULT_DISPATCH_RESPONSE);
    }

    /**
     * Advanced constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    public TrailMetricsHttpServerInterceptor(String headerName) {
        super(headerName, TrailBehaviourMode.LENIENT, DEFAULT_FOLLOW_SESSIONS, DEFAULT_DISPATCH_REQUEST, DEFAULT_DISPATCH_RESPONSE);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param followSessions Whether or not to use the same correlationId for requests of the same session that do not
     *                       have a specific correlation ID set in their header.
     * @param dispatchRequest Whether or not to dispatch an event when a request begins.
     * @param dispatchResponse Whether or not to dispatch an event when a request is responded to.
     */
    @Autowired
    public TrailMetricsHttpServerInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                             @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode,
                                             @Value("${"+ PRTY_FOLLOW_SESSIONS +":"+ DEFAULT_FOLLOW_SESSIONS +"}") boolean followSessions,
                                             @Value("${"+ PRTY_DISPATCH_REQUEST +":"+ DEFAULT_DISPATCH_REQUEST +"}") boolean dispatchRequest,
                                             @Value("${"+ PRTY_DISPATCH_RESPONSE +":"+ DEFAULT_DISPATCH_RESPONSE +"}") boolean dispatchResponse) {
        super(headerName, TrailBehaviourMode.valueOf(incomingMode), followSessions, dispatchRequest, dispatchResponse);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param followSessions Whether or not to use the same correlationId for requests of the same session that do not
     *                       have a specific correlation ID set in their header.
     * @param dispatchRequest Whether or not to dispatch an event when a request begins.
     * @param dispatchResponse Whether or not to dispatch an event when a request is responded to.
     */
    public TrailMetricsHttpServerInterceptor(String headerName, TrailBehaviourMode incomingMode, boolean followSessions,
                                             boolean dispatchRequest, boolean dispatchResponse) {
        super(headerName, incomingMode, followSessions, dispatchRequest, dispatchResponse);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        requestStart(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        requestEnd(response);
    }
}
