# trail-measurements-adaptor-spring-boot-jpa

Contains a Spring Boot auto configuration that automatically provides adaptor-JPA's **_MetricsPersistor_** bean and migrates the database to contain the necessary tables.

## How to use

The **_TrailMetricsHibernateJpaAutoConfiguration_** will activate automatically if an **_EntityManager_** bean is available.

## Config

```yaml
trailMetrics:
  jpa:
    doMigrate: <Whether or not to migrate the database to have tables for storing measurements, true by default>
    doCleanup: <Whether or not to remove old measurements from the database by a scheduled task, false by default>
    cleanup:
      cron: <The cron expression to determine how often to run the cleanup task, every full hour by default>
      age: <A duration in Spring Boot (1ms, 1s, 1m, 1h, 1d) or ISO-8601 format age for measurements until they are cleaned, 7 days by default>
      identifiersLike: <An array of SQL LIKE pattern matchers; % by default>
      doLock: <Whether or not to lock the database for the cleanup, in case the task is executed by multiple applications, false by default>
      lock:
        atLeastFor: <A duration in Spring Boot (1ms, 1s, 1m, 1h, 1d) or ISO-8601 format min lock duration, 5m by default>
        atMostFor: <A duration in Spring Boot (1ms, 1s, 1m, 1h, 1d) or ISO-8601 format max lock duration, 1h by default>
    dataSourceQualifier: <The Spring bean qualifier of the DataSource to use if there are multiple>
```