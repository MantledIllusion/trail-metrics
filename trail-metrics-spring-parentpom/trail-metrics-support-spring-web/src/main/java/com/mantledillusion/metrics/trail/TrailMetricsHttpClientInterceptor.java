package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * {@link ClientHttpRequestInterceptor} that will add the calling {@link Thread} {@link MetricsTrail}'s ID as a HTTP
 * header to any outgoing request.
 * <p>
 * Use {@link TrailMetricsHttpClientInterceptor#TrailMetricsHttpClientInterceptor(String)} or the {@value #PRTY_HEADER_NAME}
 * property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 */
public class TrailMetricsHttpClientInterceptor implements ClientHttpRequestInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.http.trailIdHeaderName";
    public static final String DEFAULT_HEADER_NAME = "trailId";

    private final String headerName;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsHttpClientInterceptor() {
        this(DEFAULT_HEADER_NAME);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    @Autowired
    public TrailMetricsHttpClientInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (MetricsTrailSupport.has()) {
            request.getHeaders().add(this.headerName, MetricsTrailSupport.get().toString());
        }
        return execution.execute(request, body);
    }
}
