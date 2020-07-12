package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;

/**
 * {@link ApplicationListener} that will write an {@link MetricType#ALERT} {@link Metric} with the ID
 * {@value #MID_AUTHENTICATION_FAILURE} when a principal's authentication fails.
 * <p>
 * Attributes:<br>
 * - {@value #AKEY_PRINCIPAL_NAME}: The name of the principal whose authentication failed.<br>
 * - {@value #AKEY_FAILURE_MESSAGE}: The message of the {@link AbstractAuthenticationFailureEvent}.<br>
 */
public class TrailMetricsSecurityAuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    public static final String MID_AUTHENTICATION_FAILURE = "spring.security.auth.failure";
    public static final String AKEY_PRINCIPAL_NAME = "principalName";
    public static final String AKEY_FAILURE_MESSAGE = "failureMessage";

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        Metric metric = new Metric(MID_AUTHENTICATION_FAILURE, MetricType.ALERT);
        metric.getAttributes().add(new MetricAttribute(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName()));
        metric.getAttributes().add(new MetricAttribute(AKEY_FAILURE_MESSAGE, event.getException().getMessage()));
        MetricsTrailSupport.commit(metric, false);
    }
}
