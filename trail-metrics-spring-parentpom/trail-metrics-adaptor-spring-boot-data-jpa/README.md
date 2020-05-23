# trail-metrics-adaptor-spring-boot-jpa

Contains a Spring Boot auto configuration that automatically provides adaptor-JPA's **_MetricsPersistor_** bean and migrates the database to contain the necessary tables.

## How to use

The **_TrailMetricsHibernateJpaAutoConfiguration_** will activate automatically if an **_EntityManager_** bean is available.