package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.core.Authentication;

import java.util.Arrays;

/**
 * AspectJ @{@link Aspect} that intercepts {@link org.springframework.security.authentication.AuthenticationProvider#authenticate(Authentication)}
 * to dispatch events when a provider attempts authentication.
 * <p>
 * Requires Spring AOP's @{@link EnableAspectJAutoProxy} to be active.
 */
@Aspect
public class TrailMetricsSecurityAuthenticationProviderInterceptor {

    private static final String ASPECTJ_SPRING_AUTHENTICATION_MANAGER_METHOD_MATCHER = "execution(* org.springframework.security.authentication.AuthenticationProvider.authenticate(org.springframework.security.core.Authentication))";
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    private static final String MID_AUTHENTICATION = "spring.security.provider";
    private static final String AKEY_AUTHENTICATION_PROVIDER = "authenticationProvider";
    private static final String AKEY_PRINCIPAL_NAME = "principalName";
    private static final String AKEY_DURATION = "duration";
    private static final String AKEY_SUCCESS = "success";
    private static final String AKEY_FAILURE_MESSAGE = "failureMessage";

    public static final String PRTY_DISPATCH_SUCCESS = "trailMetrics.security.provider.dispatch";
    public static final boolean DEFAULT_DISPATCH_SUCCESS = false;

    @Value("${"+PRTY_DISPATCH_SUCCESS+":"+DEFAULT_DISPATCH_SUCCESS+"}")
    private boolean dispatch;

    @Before(ASPECTJ_SPRING_AUTHENTICATION_MANAGER_METHOD_MATCHER)
    public void before() {
        THREAD_LOCAL.set(System.currentTimeMillis());
    }

    @AfterReturning(value = ASPECTJ_SPRING_AUTHENTICATION_MANAGER_METHOD_MATCHER, returning = "authentication")
    private void afterReturning(JoinPoint joinPoint, Authentication authentication) {
        if (authentication != null) {
            dispatch(joinPoint, authentication, true);
        }
    }

    @AfterThrowing(value = ASPECTJ_SPRING_AUTHENTICATION_MANAGER_METHOD_MATCHER, throwing = "e")
    private void afterThrowing(JoinPoint joinPoint, Exception e) {
        dispatch(joinPoint, (Authentication) joinPoint.getArgs()[0], false,
                new Measurement(AKEY_FAILURE_MESSAGE, e.getMessage(), MeasurementType.STRING));
    }

    private void dispatch(JoinPoint joinPoint, Authentication authentication, boolean success, Measurement... measurements) {
        if (this.dispatch) {
            Event event = new Event();
            event.setIdentifier(MID_AUTHENTICATION);
            event.getMeasurements().add(new Measurement(AKEY_AUTHENTICATION_PROVIDER, joinPoint.getThis().getClass().getName(), MeasurementType.STRING));
            event.getMeasurements().add(new Measurement(AKEY_PRINCIPAL_NAME, authentication.getName(), MeasurementType.STRING));
            event.getMeasurements().add(new Measurement(AKEY_DURATION, String.valueOf(System.currentTimeMillis()-THREAD_LOCAL.get()), MeasurementType.LONG));
            event.getMeasurements().add(new Measurement(AKEY_SUCCESS, String.valueOf(success), MeasurementType.BOOLEAN));
            event.getMeasurements().addAll(Arrays.asList(measurements));

            MetricsTrailSupport.commit(event);
        }
    }
}
