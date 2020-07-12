package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

/**
 * {@link ApplicationListener} that will write an {@link MetricType#ALERT} {@link Metric} with the ID
 * {@value #MID_AUTHENTICATION_SUCCESS} when a principal authenticates successfully.
 * <p>
 * Attributes:<br>
 * - {@value #AKEY_PRINCIPAL_NAME}: The name of the principal that authenticated.<br>
 */
public class TrailMetricsSecurityAuthenticationSuccessListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    public static final String MID_AUTHENTICATION_SUCCESS = "spring.security.auth.success";
    public static final String AKEY_PRINCIPAL_NAME = "principalName";

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        Metric metric = new Metric(MID_AUTHENTICATION_SUCCESS, MetricType.ALERT);
        metric.getAttributes().add(new MetricAttribute(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName()));
        MetricsTrailSupport.commit(metric, false);
    }
}
