# trail-measurements-support-spring-web

Contains a Spring Feign interceptor for automatically adding a **_MetricTrail_**'s ID as a HTTP header when calling a service.

## How to use

The **_TrailMetricsFeignAutoConfiguration_** will add a  **_TrailMetricsFeignRequestInterceptor_** to Feign clients, which automatically adds a **_MetricTrail_**'s ID as a HTTP header when calling a service.

## Config

```yaml
trailMetrics:
  http:
    correlationIdHeaderName: <The name of the HTTP header to use when sending correlationIds, 'correlation-id' by default>
    outgoingMode: <The mode of how to handle outgoing trails, one of [STRICT, LENIENT, OPTIONAL], OPTIONAL by default>
```