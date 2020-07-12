package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

/**
 * {@link ApplicationListener} that will write an {@link MetricType#ALERT} {@link Metric} with the ID
 * {@value #MID_AUTHENTICATION_CLOSURE} when a principal's authentication ends.
 * <p>
 * Attributes:<br>
 * - {@value #AKEY_PRINCIPAL_NAME}: The name of the principal whose authentication ended.<br>
 */
public class TrailMetricsSecurityAuthenticationClosureListener implements ApplicationListener<LogoutSuccessEvent> {

    public static final String MID_AUTHENTICATION_CLOSURE = "spring.security.auth.closure";
    public static final String AKEY_PRINCIPAL_NAME = "principalName";

    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
        Metric metric = new Metric(MID_AUTHENTICATION_CLOSURE, MetricType.ALERT);
        metric.getAttributes().add(new MetricAttribute(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName()));
        MetricsTrailSupport.commit(metric, false);
    }
}
