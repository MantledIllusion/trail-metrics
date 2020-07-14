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

    @Value("${"+PRTY_MODE+":FILTER}")
    private String mode;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_HEADER_NAME+":"+TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME+"}")
    private String headerName;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_INCOMING_MODE+":"+TrailMetricsHttpServerInterceptor.DEFAULT_INCOMING_MODE+"}")
    private String incomingMode;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_DISPATCH_REQUEST+":"+TrailMetricsHttpServerInterceptor.DEFAULT_DISPATCH_REQUEST+"}")
    private boolean dispatchRequest;
    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_DISPATCH_RESPONSE+":"+TrailMetricsHttpServerInterceptor.DEFAULT_DISPATCH_RESPONSE+"}")
    private boolean dispatchResponse;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if ("INTERCEPTOR".equals(this.mode)) {
            registry.addInterceptor(new TrailMetricsHttpServerInterceptor(this.headerName, this.incomingMode, this.dispatchRequest, this.dispatchResponse));
        }
    }

    @Bean
    @ConditionalOnProperty(name = PRTY_MODE, havingValue = "FILTER", matchIfMissing = true)
    public FilterRegistrationBean<TrailMetricsHttpServerFilter> trailMetricsHttpServerFilter() {
        TrailMetricsHttpServerFilter filter = new TrailMetricsHttpServerFilter(this.headerName, this.incomingMode,
                this.dispatchRequest, this.dispatchResponse);

        FilterRegistrationBean<TrailMetricsHttpServerFilter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
        filterRegistrationBean.setOrder(-1);
        return filterRegistrationBean;
    }
}
