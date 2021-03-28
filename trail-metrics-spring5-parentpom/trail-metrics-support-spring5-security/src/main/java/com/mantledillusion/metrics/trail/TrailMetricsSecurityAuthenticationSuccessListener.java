package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

/**
 * {@link ApplicationListener} that will write an {@link Event} with the ID {@value #MID_AUTHENTICATION_SUCCESS} when
 * a principal authenticates successfully.
 */
public class TrailMetricsSecurityAuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    public static final String PRTY_DISPATCH_SUCCESS = "trailMetrics.security.authentication.dispatchSuccess";
    public static final boolean DEFAULT_DISPATCH_SUCCESS = false;

    private static final String MID_AUTHENTICATION_SUCCESS = "spring.security.auth.success";
    private static final String AKEY_PRINCIPAL_NAME = "principalName";

    @Value("${"+PRTY_DISPATCH_SUCCESS+":"+DEFAULT_DISPATCH_SUCCESS+"}")
    private boolean dispatch;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        if (this.dispatch) {
            Event measurement = new Event(MID_AUTHENTICATION_SUCCESS,
                    new Measurement(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName(), MeasurementType.STRING));
            MetricsTrailSupport.commit(measurement, false);
        }
    }
}
