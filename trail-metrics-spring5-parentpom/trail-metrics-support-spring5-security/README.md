# trail-metrics-support-spring-security

Contains Spring event listeners for creating events when authentications fail/begin/end.

## How to use

Register any combination of instances of the following classes as Spring beans:
- **_TrailMetricsSecurityAuthenticationFailureListener_**
- **_TrailMetricsSecurityAuthenticationSuccessListener_**
- **_TrailMetricsSecurityAuthenticationClosureListener_**

## Metrics dispatched
- ALERT spring.security.auth.failure: When an authentication fails.
- ALERT spring.security.auth.success: When an authentication succeeds.
- ALERT spring.security.auth.closure: When an authentication ends.