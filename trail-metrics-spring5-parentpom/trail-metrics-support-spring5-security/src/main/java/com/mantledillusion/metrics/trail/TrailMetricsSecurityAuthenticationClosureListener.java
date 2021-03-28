package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

/**
 * {@link ApplicationListener} that will write an {@link Event} with the ID {@value #MID_AUTHENTICATION_CLOSURE} when
 * a principal's authentication ends.
 */
public class TrailMetricsSecurityAuthenticationClosureListener implements ApplicationListener<LogoutSuccessEvent> {

    public static final String PRTY_DISPATCH_CLOSURE = "trailMetrics.security.authentication.dispatchClosure";
    public static final boolean DEFAULT_DISPATCH_CLOSURE = false;

    private static final String MID_AUTHENTICATION_CLOSURE = "spring.security.auth.closure";
    private static final String AKEY_PRINCIPAL_NAME = "principalName";

    @Value("${"+PRTY_DISPATCH_CLOSURE+":"+DEFAULT_DISPATCH_CLOSURE+"}")
    private boolean dispatch;

    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
        if (this.dispatch) {
            Event measurement = new Event(MID_AUTHENTICATION_CLOSURE,
                    new Measurement(AKEY_PRINCIPAL_NAME, event.getAuthentication().getName(), MeasurementType.STRING));
            MetricsTrailSupport.commit(measurement, false);
        }
    }
}
