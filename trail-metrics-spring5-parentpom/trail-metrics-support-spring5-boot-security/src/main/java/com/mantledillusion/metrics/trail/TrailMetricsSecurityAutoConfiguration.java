package com.mantledillusion.metrics.trail;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto configuration registering the following {@link org.springframework.context.ApplicationListener}s:<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}<br>
 */
@Configuration
@AutoConfigureAfter(SecurityAutoConfiguration.class)
public class TrailMetricsSecurityAutoConfiguration {

    @Bean
    public TrailMetricsSecurityAuthenticationFailureListener authenticationFailureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationFailureListener();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationSuccessListener authenticationSuccessMetricsListener() {
        return new TrailMetricsSecurityAuthenticationSuccessListener();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationClosureListener authenticationClosureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationClosureListener();
    }
}
