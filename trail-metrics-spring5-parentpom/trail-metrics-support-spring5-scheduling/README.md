# trail-metrics-support-spring-scheduling

Contains an interceptor for Spring _@Scheduled_ tasks to automatically begin a **_MetricTrail_** when a task begins.

## How to use

Use _@EnableAspectJAutoProxy_ on any Spring configuration and instantiate **_TrailMetricsSchedulingInterceptor_** as a bean.

## Metrics dispatched
- ALERT spring.scheduling.task.begin: When a task begins.
  - className: The task's class name
  - methodName: The @Scheduled annotated method name
- METER spring.scheduling.task.end: When a task ends.
  - operator: The task's run duration in milliseconds.