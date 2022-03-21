package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

abstract class AbstractTrailMetricsHttpServerHandler {

    private static final ThreadLocal<Long> REQUEST_DURATION = new ThreadLocal<>();

    private static final String MID_REQUEST = "spring.web.server.request";
    private static final String AKEY_ENDPOINT = "endpoint";
    private static final String AKEY_ORIGINAL_CORRELATION_ID = "originalCorrelationId";
    private static final String AKEY_DURATION = "duration";

    private static final String URI_PLACEHOLDER = "/{id}";

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.correlationIdHeaderName";
    public static final String PRTY_REQUEST_PATTERNS = "trailMetrics.http.requestPatterns";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.http.incomingMode";
    public static final String PRTY_FOLLOW_SESSIONS = "trailMetrics.http.server.followSessions";
    public static final String PRTY_DISPATCH_PATTERNS = "trailMetrics.http.dispatchPatterns";
    public static final String PRTY_DISPATCH_EVENT = "trailMetrics.http.dispatchEvent";
    public static final String PRTY_ID_MATCHERS = "trailMetrics.http.idMatchers";
    public static final String DEFAULT_HEADER_NAME = MetricsTrailSupport.DEFAULT_TRAIL_ID_KEY;
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";
    public static final boolean DEFAULT_FOLLOW_SESSIONS = true;
    public static final boolean DEFAULT_DISPATCH_EVENT = false;
    public static final String DEFAULT_URI_MATCHER_NUMID = "/\\d+";
    public static final String DEFAULT_URI_MATCHER_UUID = "/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private final String headerName;
    private List<PathPattern> requestPatterns = Collections.emptyList();
    private TrailBehaviourMode incomingMode;
    private boolean followSessions;
    private List<PathPattern> dispatchPatterns = Collections.emptyList();
    private boolean dispatchEvent;
    private List<String> idMatchers = Collections.emptyList();

    public AbstractTrailMetricsHttpServerHandler(String headerName, TrailBehaviourMode incomingMode,
                                                 boolean followSessions, boolean dispatchEvent) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
        setIncomingMode(incomingMode);
        this.followSessions = followSessions;
        this.dispatchEvent = dispatchEvent;
    }

    public List<PathPattern> getRequestPatterns() {
        return this.requestPatterns;
    }

    public void setRequestPatterns(String... requestMvcPatterns) {
        this.requestPatterns = Arrays.asList(requestMvcPatterns).stream().
                map(pattern -> new PathPatternParser().parse(pattern)).
                collect(Collectors.toList());
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

    public List<PathPattern> getDispatchPatterns() {
        return dispatchPatterns;
    }

    public void setDispatchPatterns(String... dispatchMvcPatterns) {
        this.dispatchPatterns = Arrays.stream(dispatchMvcPatterns).
                map(pattern -> new PathPatternParser().parse(pattern)).
                collect(Collectors.toList());
    }

    /**
     * Returns whether to dispatch a metric when a request comes in.
     *
     * @return True if a metric is dispatched, false otherwise.
     */
    public boolean isDispatchEvent() {
        return this.dispatchEvent;
    }

    /**
     * Sets whether to dispatch a metric when a request comes in.
     *
     * @param dispatchEvent True if a metric should be dispatched, false otherwise.
     */
    public void setDispatchEvent(boolean dispatchEvent) {
        this.dispatchEvent = dispatchEvent;
    }

    public List<String> getIdMatchers() {
        return idMatchers;
    }

    public void setIdMatchers(String... idMatchers) {
        this.idMatchers = Arrays.asList(idMatchers);
    }

    protected boolean requestStart(ServletRequest request) {
        if (matches(this.requestPatterns, request)) {
            try {
                MetricsTrailSupport.begin(UUID.fromString(((HttpServletRequest) request).getHeader(this.headerName)));
            } catch (NullPointerException | IllegalArgumentException e) {
                switch (this.incomingMode) {
                    case STRICT:
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
                    case LENIENT:
                        if (this.followSessions) {
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
                }
            }

            REQUEST_DURATION.set(System.currentTimeMillis());
        }
        return true;
    }

    protected void dispatchMetric(ServletRequest request, ServletResponse response) {
        if (matches(this.requestPatterns, request)) {
            ((HttpServletResponse) response).addHeader(this.headerName, MetricsTrailSupport.id().toString());

            if (this.dispatchEvent && matches(this.dispatchPatterns, request)) {
                String uri = ((HttpServletRequest) request).getRequestURI();
                for (String pattern: this.idMatchers) {
                    uri = uri.replaceAll(pattern, URI_PLACEHOLDER);
                }

                Event event = new Event(MID_REQUEST,
                        new Measurement(
                                AKEY_ENDPOINT,
                                uri,
                                MeasurementType.STRING),
                        new Measurement(
                                AKEY_DURATION,
                                String.valueOf(System.currentTimeMillis()-REQUEST_DURATION.get()),
                                MeasurementType.LONG)
                );

                if (((HttpServletRequest) request).getHeader(this.headerName) != null) {
                    event.getMeasurements().add(
                            new Measurement(
                                    AKEY_ORIGINAL_CORRELATION_ID,
                                    ((HttpServletRequest) request).getHeader(this.headerName),
                                    MeasurementType.STRING));
                }

                MetricsTrailSupport.commit(event, false);
            }
            REQUEST_DURATION.set(null);
        }
    }

    protected void requestEnd() {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }

    private boolean matches(List<PathPattern> patterns, ServletRequest request) {
        return request instanceof HttpServletRequest && (patterns.isEmpty() || patterns.parallelStream().anyMatch(
                pathPattern -> pathPattern.matches(RequestPath.parse(URI.create(((HttpServletRequest) request).
                                getRequestURI()), ((HttpServletRequest) request).getContextPath()))));
    }
}
