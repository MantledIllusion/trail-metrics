# trail-measurements-support-spring-boot-security

Contains a Spring Boot auto configurations that automatically configures measurement dispatchers for failing/starting/ending authentications.

## How to use

The **_TrailMetricsSecurityAutoConfiguration_** will activate automatically and enable dispatchers for the authentication provider and all three authentication event types.

## Config

```yaml
trailMetrics:
  security:
    provider:
      dispatch: <Dispatch a measurement when an authentication provider has handled authentication, false by default>
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
- **spring.security.auth.failure**: When an authentication fails.
  - principalName: The name of the principal.
  - failureMessage: The message of the exception causing the failure.
- **spring.security.auth.success**: When an authentication succeeds.
  - principalName: The name of the principal.
- **spring.security.auth.closure**: When an authentication ends.
  - principalName: The name of the principal.