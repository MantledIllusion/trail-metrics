package com.mantledillusion.metrics.trail;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;

/**
 * AspectJ @{@link Aspect} that intercepts @{@link JmsListener} endpoints by ending a {@link MetricsTrail} right after
 * the execution.
 * <p>
 * NOTE: The {@link MetricsTrail} is expected to be begun by adding a {@link TrailMetricsJmsMessageConverterWrapper} to
 * the {@link AbstractJmsListenerContainerFactory}.
 * <p>
 * Requires Spring AOP's @{@link EnableAspectJAutoProxy} to be active.
 */
@Aspect
public class TrailMetricsJmsInterceptor {

    private static final String ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER = "@annotation(org.springframework.jms.annotation.JmsListener)";

    @After(ASPECTJ_SPRING_SCHEDULED_ANNOTATION_MATCHER)
    private void after() {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }
}
