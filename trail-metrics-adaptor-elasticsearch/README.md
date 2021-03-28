# trail-measurements-adaptor-elasticsearch

A **_MetricsConsumer_** that is able to persist measurements into an ElasticSearch DB.

## How to use

Create a **_ElasticMetricsPersistor_** instance by providing an instance of **_ElasticSearch_** REST Client to _**ElasticMetricsPersistor**.from()_; the client will then be used for persisting any **_Metric_** instances incoming.