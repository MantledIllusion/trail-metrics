package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

/**
 * {@link ApplicationListener} that will write an {@link MetricType#ALERT} {@link Metric} with the ID
 * {@value #MID_AUTHENTICATION_SUCCESS} when a principal authenticates successfully.
 */
public class TrailMetricsSecurityAuthenticationSuccessListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    public static final String PRTY_DISPATCH_SUCCESS = "trailMetrics.security.authentication.dispatchSuccess";
    public static final boolean DEFAULT_DISPATCH_SUCCESS = false;

    private static final String MID_AUTHENTICATION_SUCCESS = "spring.security.auth.success";
    private static final String AKEY_PRINCIPAL_NAME = "principalName";

    @Value("${"+PRTY_DISPATCH_SUCCESS+":"+DEFAULT_DISPATCH_SUCCESS+"}")
    private boolean dispatch;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        if (this.dispatch) {
            Metric metric = new Metric(MID_AUTHENTICATION_SUCCESS, MetricType.ALERT);
            metric.getAttributes().add(new MetricAttribute(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName()));
            MetricsTrailSupport.commit(metric, false);
        }
    }
}
