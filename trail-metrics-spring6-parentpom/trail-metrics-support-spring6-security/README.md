# trail-measurements-support-spring-security

Contains Spring event listeners for creating events when authentications fail/begin/end.

## How to use

Register any combination of instances of the following classes as Spring beans:
- **_TrailMetricsSecurityAuthenticationProviderInterceptor_**
- **_TrailMetricsSecurityAuthenticationFailureListener_**
- **_TrailMetricsSecurityAuthenticationSuccessListener_**
- **_TrailMetricsSecurityAuthenticationClosureListener_**

## Config

```yaml
trailMetrics:
  security:
    provider:
      dispatch: <Dispatch a measurement when an authentication provider has handled authentication, false by default>
    manager:
      dispatch: <Dispatch a measurement when an authentication manager has handled authentication, false by default>
    authentication:
      dispatchFailure: <Dispatch a measurement when authentication fails, false by default>
      dispatchSuccess: <Dispatch a measurement when authentication succeeds, false by default>
      dispatchClosure: <Dispatch a measurement when authentication ends, false by default>
```

## Metrics dispatched
- **spring.security.provider**: When an authentication provider has handled authentication
  - authenticationProvider: The fully qualified class name of the provider
  - principalName: The name of the principal.
  - duration: The time it took for authentication, in milliseconds.
  - success: Whether authentication succeeded.
  - failureMessage: The message of the exception causing the failure.
- **spring.security.manager**: When an authentication manager has handled authentication
  - authenticationProvider: The fully qualified class name of the manager
  - principalName: The name of the principal.
  - duration: The time it took for authentication, in milliseconds.
  - success: Whether authentication succeeded.
  - failureMessage: The message of the exception causing the failure.
- **spring.security.auth.failure**: When an authentication fails.
  - principalName: The name of the principal.
  - failureMessage: The message of the exception causing the failure.
- **spring.security.auth.success**: When an authentication succeeds.
  - principalName: The name of the principal.
- **spring.security.auth.closure**: When an authentication ends.
  - principalName: The name of the principal.