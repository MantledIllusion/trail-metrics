package com.mantledillusion.metrics.trail;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan
@EnableScheduling
@EnableAspectJAutoProxy
@Import(TrailMetricsSchedulingInterceptor.class)
public class TestSchedulingConfig {
}
