# trail-measurements-support-spring-boot-scheduling

Contains a Spring Boot auto configuration that automatically configures an interceptor for @Scheduled annotated methods.

## How to use

Use _@EnableScheduling_ on any Spring configuration.

## Config

```yaml
trailMetrics:
  scheduling:
    endMode: <How to end task trails, one of [IMMEDIATE, IMMEDIATE_ON_SUCCESS, IMMEDIATE_ON_FAILURE, DELAYED], IMMEDIATE by default>
    dispatchBegin: <Dispatch a measurement when a task begins, false by default>
    dispatchEnd: <Dispatch a measurement when a task ends, false by default>
```

## Metrics dispatched
- ALERT spring.scheduling.task.begin: When a task begins.
  - className: The task's class name
  - methodName: The @Scheduled annotated method name
- METER spring.scheduling.task.end: When a task ends.
  - operator: The task's run duration in milliseconds.