package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * {@link HandlerInterceptor} that will begin a {@link MetricsTrail} on the {@link Thread} of an incoming HTTP request
 * and will end it when the request is responded to.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, TrailBehaviourMode)} or the
 * {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String, TrailBehaviourMode)} or the
 * {@value #PRTY_INCOMING_MODE} property to set the mode to use, which is {@value #DEFAULT_INCOMING_MODE} by default.
 * <p>
 * The incoming HTTP request header is according to the {@link TrailBehaviourMode} set. When returning the response,
 * the HTTP response header is set to whatever ID was used to identify the {@link MetricsTrail}.
 */
public class TrailMetricsHttpServerInterceptor implements HandlerInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.trailIdHeaderName";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.http.incomingMode";
    public static final String DEFAULT_HEADER_NAME = "trailId";
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";

    private final String headerName;
    private TrailBehaviourMode incomingMode;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsHttpServerInterceptor() {
        this(DEFAULT_HEADER_NAME, TrailBehaviourMode.LENIENT);
    }

    /**
     * Advanced constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    public TrailMetricsHttpServerInterceptor(String headerName) {
        this(headerName, TrailBehaviourMode.LENIENT);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsHttpServerInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                             @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode) {
        this(headerName, TrailBehaviourMode.valueOf(incomingMode));
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     */
    public TrailMetricsHttpServerInterceptor(String headerName, TrailBehaviourMode incomingMode) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
        setIncomingMode(incomingMode);
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            MetricsTrailSupport.begin(UUID.fromString(request.getHeader(this.headerName)));
        } catch (NullPointerException | IllegalArgumentException e) {
            switch (this.incomingMode) {
                case STRICT:
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
                case LENIENT:
                    MetricsTrailSupport.begin();
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (MetricsTrailSupport.has()) {
            response.addHeader(this.headerName, MetricsTrailSupport.get().toString());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }
}
