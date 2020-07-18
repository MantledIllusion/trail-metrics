# trail-metrics-support-spring-boot-web

Contains a Spring Boot auto configurations that automatically configures interceptors for in and outgoing web service calls.

## How to use

The **_TrailMetricsWebAutoConfiguration_** will activate automatically if a **_RestTemplate_** bean is existing, adding a  **_TrailMetricsHttpClientInterceptor_** to each RestTemplate found, which automatically adds a **_MetricTrail_**'s ID as a HTTP header when calling a service.

The **_TrailMetricsHttpServerInterceptor_** will activate automatically if a **_RestTemplate_** bean is existing, adding a  **_TrailMetricsHttpServerInterceptor_** to the WebMVC context, which automatically begins / ends a **_MetricTrail_** as a HTTP request comes in / is responded to.

## Config

```yaml
trailMetrics:
  http:
    server:
      followSessions: <Whether or not to use the same correlationId for requests of the same session that do not have a specific ID set, true by default>
      mode: <The mode how to handle request on server side, one of [FILTER, INTERCEPTOR], FILTER by default>
      filter:
        order: <When using mode:FILTER, the priority of the filter in correlation to other filters, -99000 by default>
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