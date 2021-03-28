# trail-measurements-support-spring-security

Contains Spring event listeners for creating events when authentications fail/begin/end.

## How to use

Register any combination of instances of the following classes as Spring beans:
- **_TrailMetricsSecurityAuthenticationFailureListener_**
- **_TrailMetricsSecurityAuthenticationSuccessListener_**
- **_TrailMetricsSecurityAuthenticationClosureListener_**

## Config

```yaml
trailMetrics:
  security:
    authentication:
      dispatchFailure: <Dispatch a measurement when authentication fails, false by default>
      dispatchSuccess: <Dispatch a measurement when authentication succeeds, false by default>
      dispatchClosure: <Dispatch a measurement when authentication ends, false by default>
```

## Metrics dispatched
- ALERT spring.security.auth.failure: When an authentication fails.
  - principalName: The name of the principal.
  - failureMessage: The message of the exception causing the failure.
- ALERT spring.security.auth.success: When an authentication succeeds.
  - principalName: The name of the principal.
- ALERT spring.security.auth.closure: When an authentication ends.
  - principalName: The name of the principal.