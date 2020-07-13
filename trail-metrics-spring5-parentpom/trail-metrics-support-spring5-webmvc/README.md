# trail-metrics-support-spring-webmvc

Contains a Spring WebMVC interceptor for automatically beginning / ending a **_MetricTrail_** as a HTTP request comes in / is responded to.

## How to use

Instances of **_TrailMetricsHttpServerInterceptor_** can be added to the **_InterceptorRegistry_** given when implementing _**WebMvcConfigurer**.addInterceptors()_.

## Metrics dispatched
- ALERT spring.web.server.request: When a request begins.
  - endpoint: The endpoint the request was targeted at.
  - originalCorrelationId: The correlation ID of the incoming request if it could not be used.
- METER spring.web.server.response: When a request ends.
  - operator: The request's run duration in milliseconds.