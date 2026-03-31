package com.mantledillusion.metrics.trail;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsSecurityAuthenticationProviderInterceptor} and
 * a {@link TrailMetricsSecurityAuthenticationManagerInterceptor}.
 * <p>
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
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationProviderInterceptor.PRTY_DISPATCH_SUCCESS)
    public TrailMetricsSecurityAuthenticationProviderInterceptor authenticationProviderInterceptor() {
        return new TrailMetricsSecurityAuthenticationProviderInterceptor();
    }

    @Bean
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationManagerInterceptor.PRTY_DISPATCH_SUCCESS)
    public TrailMetricsSecurityAuthenticationManagerInterceptor authenticationManagerInterceptor() {
        return new TrailMetricsSecurityAuthenticationManagerInterceptor();
    }

    @Bean
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationFailureListener.PRTY_DISPATCH_FAILURE)
    public TrailMetricsSecurityAuthenticationFailureListener authenticationFailureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationFailureListener();
    }

    @Bean
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationSuccessListener.PRTY_DISPATCH_SUCCESS)
    public TrailMetricsSecurityAuthenticationSuccessListener authenticationSuccessMetricsListener() {
        return new TrailMetricsSecurityAuthenticationSuccessListener();
    }

    @Bean
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationInteractiveSuccessListener.PRTY_DISPATCH_SUCCESS)
    public TrailMetricsSecurityAuthenticationInteractiveSuccessListener authenticationInteractiveSuccessListener() {
        return new TrailMetricsSecurityAuthenticationInteractiveSuccessListener();
    }

    @Bean
    @ConditionalOnProperty(TrailMetricsSecurityAuthenticationClosureListener.PRTY_DISPATCH_CLOSURE)
    public TrailMetricsSecurityAuthenticationClosureListener authenticationClosureMetricsListener() {
        return new TrailMetricsSecurityAuthenticationClosureListener();
    }
}
