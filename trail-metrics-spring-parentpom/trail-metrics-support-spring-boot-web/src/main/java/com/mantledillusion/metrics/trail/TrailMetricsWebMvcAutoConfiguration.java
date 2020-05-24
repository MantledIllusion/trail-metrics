package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsHttpServerInterceptor}
 */
@Configuration
@ConditionalOnBean(Servlet.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class TrailMetricsWebMvcAutoConfiguration implements WebMvcConfigurer {

    @Value("${"+TrailMetricsHttpServerInterceptor.PRTY_HEADER_NAME+":"+TrailMetricsHttpServerInterceptor.DEFAULT_HEADER_NAME+"}")
    private String headerName;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TrailMetricsHttpServerInterceptor(this.headerName));
    }
}
