package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsHttpServerInterceptor}
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class TrailMetricsWebMvcAutoConfiguration implements WebMvcConfigurer {

    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_HEADER_NAME+":"+TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME+"}")
    private String headerName;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_INCOMING_MODE+":"+TrailMetricsHttpServerInterceptor.DEFAULT_INCOMING_MODE+"}")
    String incomingMode;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_DISPATCH_REQUEST+":"+TrailMetricsHttpServerInterceptor.DEFAULT_DISPATCH_REQUEST+"}")
    boolean dispatchRequest;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_DISPATCH_RESPONSE+":"+TrailMetricsHttpServerInterceptor.DEFAULT_DISPATCH_RESPONSE+"}")
    boolean dispatchResponse;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TrailMetricsHttpServerInterceptor(this.headerName, this.incomingMode, this.dispatchRequest, this.dispatchResponse));
    }
}
