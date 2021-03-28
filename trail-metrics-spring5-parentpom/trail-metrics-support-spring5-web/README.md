# trail-measurements-support-spring-web

Contains a Spring Web interceptor for automatically adding a **_MetricTrail_**'s ID as a HTTP header when calling a service.

## How to use

Add an instance of **_TrailMetricsHttpClientInterceptor_** to the outgoing service, for example a **_RestTemplate_**.

## Config

```yaml
trailMetrics:
  http:
    correlationIdHeaderName: <The name of the HTTP header to use when sending correlationIds, 'correlation-id' by default>
    outgoingMode: <The mode of how to handle outgoing trails, one of [STRICT, LENIENT, OPTIONAL], OPTIONAL by default>
```