package com.mantledillusion.metrics.trail;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfiguration;

@Configuration
@EnableAspectJAutoProxy
@Import(TrailMetricsSchedulingInterceptor.class)
@AutoConfigureAfter(SchedulingConfiguration.class)
@ConditionalOnBean(ScheduledAnnotationBeanPostProcessor.class)
public class TrailMetricsSchedulingAutoConfiguration {

}