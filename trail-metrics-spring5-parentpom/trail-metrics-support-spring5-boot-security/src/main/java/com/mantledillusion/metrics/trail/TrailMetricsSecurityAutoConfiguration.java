package com.mantledillusion.metrics.trail;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring Boot auto configuration registering an {@link TrailMetricsSecurityAuthenticationProviderInterceptor}.
 *
 * Also configures the following {@link org.springframework.context.ApplicationListener}s:<br>
 * - {@link TrailMetricsSecurityAuthenticationFailureListener}<br>
 * - {@link TrailMetricsSecurityAuthenticationSuccessListener}<br>
 * - {@link TrailMetricsSecurityAuthenticationInteractiveSuccessListener}<br>
 * - {@link TrailMetricsSecurityAuthenticationClosureListener}<br>
 */
@Configuration
@EnableAspectJAutoProxy
@AutoConfigureAfter(SecurityAutoConfiguration.class)
public class TrailMetricsSecurityAutoConfiguration {

    @Bean
    public TrailMetricsSecurityAuthenticationProviderInterceptor authenticationProviderInterceptor() {
        return new TrailMetricsSecurityAuthenticationProviderInterceptor();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationFailureListener authenticationFailureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationFailureListener();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationSuccessListener authenticationSuccessMetricsListener() {
        return new TrailMetricsSecurityAuthenticationSuccessListener();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationInteractiveSuccessListener authenticationInteractiveSuccessListener() {
        return new TrailMetricsSecurityAuthenticationInteractiveSuccessListener();
    }

    @Bean
    public TrailMetricsSecurityAuthenticationClosureListener authenticationClosureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationClosureListener();
    }
}
