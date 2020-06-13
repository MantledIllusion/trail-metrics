# trail-metrics-support-spring-scheduling

Contains an interceptor for Spring _@Scheduled_ tasks to automatically begin a **_MetricTrail_** when a task begins.

## How to use

Use _@EnableAspectJAutoProxy_ on any Spring configuration and instantiate **_TrailMetricsSchedulingInterceptor_** as a bean.