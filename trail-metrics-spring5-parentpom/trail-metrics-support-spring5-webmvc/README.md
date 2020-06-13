# trail-metrics-support-spring-webmvc

Contains a Spring WebMVC interceptor for automatically beginning / ending a **_MetricTrail_** as a HTTP request comes in / is responded to.

## How to use

Instances of **_TrailMetricsHttpServerInterceptor_** can be added to the **_InterceptorRegistry_** given when implementing _**WebMvcConfigurer**.addInterceptors()_.