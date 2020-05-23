# trail-metrics-support-spring-web

Contains a Spring Web interceptor for automatically adding a **_MetricTrail_**'s ID as a HTTP header when calling a service.

## How to use

Add an instance of **_TrailMetricsHttpClientInterceptor_** to the outgoing service, for example a **_RestTemplate_**.