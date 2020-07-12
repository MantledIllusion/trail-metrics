package com.mantledillusion.metrics.trail;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto configuration registering the following {@link org.springframework.context.ApplicationListener}s:<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}; set {@value #PRTY_DISPATCH_FAILURE} to false to deactivate<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}; set {@value #PRTY_DISPATCH_SUCCESS} to false to deactivate<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}; set {@value #PRTY_DISPATCH_CLOSURE} to false to deactivate<br>
 */
@Configuration
@AutoConfigureAfter(SecurityAutoConfiguration.class)
public class TrailMetricsSecurityAutoConfiguration {

    public static final String PRTY_DISPATCH_FAILURE = "trailMetrics.security.authentication.dispatchFailure";
    public static final String PRTY_DISPATCH_SUCCESS = "trailMetrics.security.authentication.dispatchSuccess";
    public static final String PRTY_DISPATCH_CLOSURE = "trailMetrics.security.authentication.dispatchClosure";

    @Bean
    @ConditionalOnProperty(name = PRTY_DISPATCH_FAILURE, matchIfMissing = true)
    public TrailMetricsSecurityAuthenticationFailureListener authenticationFailureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationFailureListener();
    }

    @Bean
    @ConditionalOnProperty(name = PRTY_DISPATCH_SUCCESS, matchIfMissing = true)
    public TrailMetricsSecurityAuthenticationSuccessListener authenticationSuccessMetricsListener() {
        return new TrailMetricsSecurityAuthenticationSuccessListener();
    }

    @Bean
    @ConditionalOnProperty(name = PRTY_DISPATCH_CLOSURE, matchIfMissing = true)
    public TrailMetricsSecurityAuthenticationClosureListener authenticationClosureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationClosureListener();
    }
}
