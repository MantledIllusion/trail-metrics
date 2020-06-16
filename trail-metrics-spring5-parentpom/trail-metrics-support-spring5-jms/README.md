# trail-metrics-support-spring-jms

Contains a wrapper for Spring JMS message converters for automatically adding a **_MetricTrail_**'s ID as the JMS correlation ID header when an incoming / outgoing message is converted.

## How to use

For outgoing messages, wrap your **_MessageConverter_** instance using an instance of **_TrailMetricsJmsMessageConverterWrapper_** and add the wrapper to your **_JmsTemplate_** to add the trail ID to outgoing messages.

For incoming messages...:
- ...on _@JmsListener_ annotated methods, add your wrapped **_MessageConverter_** to your **_AbstractJmsListenerContainerFactory_** implementation and add a **_TrailMetricsJmsInterceptor_** instance as a bean to your Spring context...
- ...on custom **_JmsListener_** implementations, wrap your implementation into a **_TrailMetricsListenerWrapper_** instance...
...to extract the trail ID from an incoming message and automatically start / end a trail.