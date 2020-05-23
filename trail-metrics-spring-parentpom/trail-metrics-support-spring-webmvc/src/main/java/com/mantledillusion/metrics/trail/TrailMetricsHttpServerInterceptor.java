package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * {@link HandlerInterceptor} that will begin a {@link MetricsTrail} on the {@link Thread} of an incoming HTTP request
 * and will end it when the request is responded to.
 * <p>
 * Use {@link TrailMetricsHttpServerInterceptor#TrailMetricsHttpServerInterceptor(String)} or the
 * {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * If the HTTP request header is set and contains a UUID, it will be used to begin the {@link MetricsTrail} with; if
 * not, a {@link UUID#randomUUID()} is used. When returning the response, the HTTP response header is set to whatever
 * ID was used to identify the {@link MetricsTrail}.
 */
public class TrailMetricsHttpServerInterceptor implements HandlerInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.trailIdHeaderName";
    public static final String DEFAULT_HEADER_NAME = "trailId";

    private final String headerName;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsHttpServerInterceptor() {
        this(DEFAULT_HEADER_NAME);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    @Autowired
    public TrailMetricsHttpServerInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UUID trailId;
        try {
            trailId = UUID.fromString(request.getHeader(this.headerName));
        } catch (NullPointerException | IllegalArgumentException e) {
            trailId = UUID.randomUUID();
        }
        MetricsTrailSupport.begin(trailId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        response.addHeader(this.headerName, MetricsTrailSupport.get().toString());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MetricsTrailSupport.end();
    }
}
