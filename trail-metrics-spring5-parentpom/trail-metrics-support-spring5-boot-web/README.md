# trail-metrics-support-spring-boot-web

Contains a Spring Boot auto configurations that automatically configures interceptors for in and outgoing web service calls.

## How to use

The **_TrailMetricsWebAutoConfiguration_** will activate automatically if a **_RestTemplate_** bean is existing, adding a  **_TrailMetricsHttpClientInterceptor_** to each RestTemplate found, which automatically adds a **_MetricTrail_**'s ID as a HTTP header when calling a service.

The **_TrailMetricsHttpServerInterceptor_** will activate automatically if a **_RestTemplate_** bean is existing, adding a  **_TrailMetricsHttpServerInterceptor_** to the WebMVC context, which automatically begins / ends a **_MetricTrail_** as a HTTP request comes in / is responded to.