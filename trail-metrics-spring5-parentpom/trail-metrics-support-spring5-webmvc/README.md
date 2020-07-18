# trail-metrics-support-spring-webmvc

Contains a Spring WebMVC interceptor for automatically beginning / ending a **_MetricTrail_** as a HTTP request comes in / is responded to.

## How to use

An instance of **_TrailMetricsHttpServerInterceptor_** can be added to the **_InterceptorRegistry_** given when implementing _**WebMvcConfigurer**.addInterceptors()_.

Alternatively, an instance of **_TrailMetricsHttpServerFilter_** can be added to the servlet as a more native approach.

## Config

```yaml
trailMetrics:
  http:
    server:
      followSessions: <Whether or not to use the same correlationId for requests of the same session that do not have a specific ID set, true by default>
    correlationIdHeaderName: <The name of the HTTP header to use when sending correlationIds, 'correlationId' by default>
    requestPatterns: <An array of MVC URI patterns to limit beginning trails on to>
    incomingMode: <The mode of how to handle incoming trails, one of [STRICT, LENIENT, OPTIONAL], LENIENT by default>
    dispatchPatterns: <An array of MVC URI patterns to limit dispatching metrics to>
    dispatchRequest: <Whether or not to dispatch a metric when a request is received, false by default>
    dispatchResponse: <Whether or not to dispatch a metric when a request is responded to, false by default>
```

## Metrics dispatched
- ALERT spring.web.server.request: When a request begins.
  - endpoint: The endpoint the request was targeted at.
  - originalCorrelationId: The correlation ID of the incoming request if it could not be used.
- METER spring.web.server.response: When a request ends.
  - operator: The request's run duration in milliseconds.