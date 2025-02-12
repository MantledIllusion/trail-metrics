package com.mantledillusion.metrics.trail;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsFeignRequestInterceptor} to Feign clients.
 */
@Configuration
@AutoConfigureAfter(FeignAutoConfiguration.class)
public class TrailMetricsFeignAutoConfiguration {

    @Value("${"+TrailMetricsFeignRequestInterceptor.PRTY_HEADER_NAME+":"+TrailMetricsFeignRequestInterceptor.DEFAULT_HEADER_NAME+"}")
    private String headerName;
    @Value("${"+TrailMetricsFeignRequestInterceptor.PRTY_OUTGOING_MODE+":"+TrailMetricsFeignRequestInterceptor.DEFAULT_OUTGOING_MODE+"}")
    private String outgoingMode;

    @Bean
    public RequestInterceptor metricsRequestInterceptor() {
        return new TrailMetricsFeignRequestInterceptor(this.headerName, this.outgoingMode);
    }
}