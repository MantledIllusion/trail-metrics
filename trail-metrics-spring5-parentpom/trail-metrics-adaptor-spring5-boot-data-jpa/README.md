# trail-metrics-adaptor-spring-boot-jpa

Contains a Spring Boot auto configuration that automatically provides adaptor-JPA's **_MetricsPersistor_** bean and migrates the database to contain the necessary tables.

## How to use

The **_TrailMetricsHibernateJpaAutoConfiguration_** will activate automatically if an **_EntityManager_** bean is available.

## Config

```yaml
trailMetrics:
  jpa:
    doMigrate: <Whether or not to migrate the database to have tables for storing metrics, true by default>
    dataSourceQualifier: <The Spring bean qualifier of the DataSource to use if there are multiple>
```