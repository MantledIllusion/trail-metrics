package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;

/**
 * {@link ApplicationListener} that will write an {@link Event} with the ID {@value #MID_AUTHENTICATION_FAILURE} when
 * a principal's authentication fails.
 */
public class TrailMetricsSecurityAuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    public static final String PRTY_DISPATCH_FAILURE = "trailMetrics.security.authentication.dispatchFailure";
    public static final boolean DEFAULT_DISPATCH_FAILURE = false;

    private static final String MID_AUTHENTICATION_FAILURE = "spring.security.auth.failure";
    private static final String AKEY_PRINCIPAL_NAME = "principalName";
    private static final String AKEY_FAILURE_MESSAGE = "failureMessage";

    @Value("${"+PRTY_DISPATCH_FAILURE+":"+DEFAULT_DISPATCH_FAILURE+"}")
    private boolean dispatch;

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        if (this.dispatch) {
            Event measurement = new Event(MID_AUTHENTICATION_FAILURE,
                    new Measurement(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName(), MeasurementType.STRING),
                    new Measurement(AKEY_FAILURE_MESSAGE, event.getException().getMessage(), MeasurementType.STRING));
            MetricsTrailSupport.commit(measurement, false);
        }
    }
}
