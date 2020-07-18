package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsHttpServerInterceptor}
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class TrailMetricsWebMvcAutoConfiguration implements WebMvcConfigurer {

    public static final String PRTY_MODE = "trailMetrics.http.server.mode";
    public static final String PRTY_FILTER_ORDER = "trailMetrics.http.server.filter.order";

    @Value("${"+PRTY_MODE+":FILTER}")
    private String mode;
    @Value("${"+PRTY_FILTER_ORDER+":-99000}")
    private int filterOrder;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_HEADER_NAME+":"+AbstractTrailMetricsHttpServerHandler.DEFAULT_HEADER_NAME+"}")
    private String headerName;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_REQUEST_PATTERNS+":}")
    private String[] requestPatterns;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_INCOMING_MODE+":"+AbstractTrailMetricsHttpServerHandler.DEFAULT_INCOMING_MODE+"}")
    private String incomingMode;
    @Value("${"+ AbstractTrailMetricsHttpServerHandler.PRTY_FOLLOW_SESSIONS+":"+AbstractTrailMetricsHttpServerHandler.DEFAULT_FOLLOW_SESSIONS +"}")
    private boolean followSessions;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_DISPATCH_PATTERNS+":}")
    private String[] dispatchPatterns;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_DISPATCH_REQUEST+":"+AbstractTrailMetricsHttpServerHandler.DEFAULT_DISPATCH_REQUEST+"}")
    private boolean dispatchRequest;
    @Value("${"+AbstractTrailMetricsHttpServerHandler.PRTY_DISPATCH_RESPONSE+":"+AbstractTrailMetricsHttpServerHandler.DEFAULT_DISPATCH_RESPONSE+"}")
    private boolean dispatchResponse;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if ("INTERCEPTOR".equals(this.mode)) {
            registry.addInterceptor(new TrailMetricsHttpServerInterceptor(this.headerName, this.requestPatterns,
                    this.incomingMode, this.followSessions, this.dispatchPatterns, this.dispatchRequest, this.dispatchResponse));
        }
    }

    @Bean
    @ConditionalOnProperty(name = PRTY_MODE, havingValue = "FILTER", matchIfMissing = true)
    public FilterRegistrationBean<TrailMetricsHttpServerFilter> trailMetricsHttpServerFilter() {
        TrailMetricsHttpServerFilter filter = new TrailMetricsHttpServerFilter(this.headerName, this.requestPatterns,
                this.incomingMode, this.followSessions, this.dispatchPatterns, this.dispatchRequest, this.dispatchResponse);

        FilterRegistrationBean<TrailMetricsHttpServerFilter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
        filterRegistrationBean.setOrder(this.filterOrder);
        return filterRegistrationBean;
    }
}
