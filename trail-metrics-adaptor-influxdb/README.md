# trail-measurements-adaptor-influxdb

A **_MetricsConsumer_** that is able to persist measurements into an Influx DB.

## How to use

Create an **_InfluxMetricsPersistor_** instance by providing an instance of **_InfluxDB_** to _**InfluxMetricsPersistor**.from()_; the db will then be used for persisting any **_Metric_** instances incoming.