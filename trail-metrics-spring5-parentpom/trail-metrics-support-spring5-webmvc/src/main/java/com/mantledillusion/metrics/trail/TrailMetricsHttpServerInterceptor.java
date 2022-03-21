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
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, TrailBehaviourMode, boolean, boolean)}
 * or the {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, String[], String, boolean, String[], boolean, String[])}
 * or the {@value #PRTY_INCOMING_MODE} property to set the mode to use, which is {@value #DEFAULT_INCOMING_MODE} by default.
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
        super(DEFAULT_HEADER_NAME, TrailBehaviourMode.LENIENT, DEFAULT_FOLLOW_SESSIONS, DEFAULT_DISPATCH_EVENT);
    }

    /**
     * Advanced constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    public TrailMetricsHttpServerInterceptor(String headerName) {
        super(headerName, TrailBehaviourMode.LENIENT, DEFAULT_FOLLOW_SESSIONS, DEFAULT_DISPATCH_EVENT);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param requestPatterns The MVC URI patterns to limit beginning trails on to; might <b>not</b> be null.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param followSessions Whether to use the same correlationId for requests of the same session that do not have a specific correlation ID set in their header.
     * @param dispatchPatterns The MVC URI patterns to limit dispatching metrics to; might <b>not</b> be null.
     * @param dispatchEvent Whether to dispatch an event when a request is handled.
     * @param idMatchers The regex matchers for IDs within the URI to substitute with a placeholder; might <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsHttpServerInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                             @Value("${"+ PRTY_REQUEST_PATTERNS +":}") String[] requestPatterns,
                                             @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode,
                                             @Value("${"+ PRTY_FOLLOW_SESSIONS +":"+ DEFAULT_FOLLOW_SESSIONS +"}") boolean followSessions,
                                             @Value("${"+ PRTY_DISPATCH_PATTERNS +":}") String[] dispatchPatterns,
                                             @Value("${"+ PRTY_DISPATCH_EVENT +":"+ DEFAULT_DISPATCH_EVENT +"}") boolean dispatchEvent,
                                             @Value("${"+ PRTY_ID_MATCHERS +":}") String[] idMatchers) {
        super(headerName, TrailBehaviourMode.valueOf(incomingMode), followSessions, dispatchEvent);
        setRequestPatterns(requestPatterns);
        setDispatchPatterns(dispatchPatterns);
        setIdMatchers(idMatchers);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param followSessions Whether to use the same correlationId for requests of the same session that do not have a specific correlation ID set in their header.
     * @param dispatchRequest Whether to dispatch an event when a request begins.
     */
    public TrailMetricsHttpServerInterceptor(String headerName, TrailBehaviourMode incomingMode,
                                             boolean followSessions, boolean dispatchRequest) {
        super(headerName, incomingMode, followSessions, dispatchRequest);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        requestStart(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        dispatchMetric(request, response);
        requestEnd();
    }
}
