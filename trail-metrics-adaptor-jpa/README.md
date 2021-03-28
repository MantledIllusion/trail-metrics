# trail-measurements-adaptor-jpa

A **_MetricsConsumer_** that is able to persist measurements using JPA2 annotated POJOs.

## How to use

Create a **_MetricsPersistor_** instance by providing an instance of **_EntityManager_** to _**MetricsPersistor**.from()_; the manager will then be used for persisting any **_Metric_** instances incoming.

If desired, the file _resources/init_metrics_schema.sql_ can be used to migrate SQL databases to contain the required tables to persist into.