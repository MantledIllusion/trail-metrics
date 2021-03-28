# trail-measurements-support-spring-boot-security

Contains a Spring Boot auto configurations that automatically configures measurement dispatchers for failing/starting/ending authentications.

## How to use

The **_TrailMetricsSecurityAutoConfiguration_** will activate automatically and enable dispatchers for all three cases.

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