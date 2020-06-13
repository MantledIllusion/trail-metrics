# trail-metrics-support-spring-jms

Contains a wrapper for Spring JMS message converters for automatically adding a **_MetricTrail_**'s ID as the JMS correlation ID header when an incoming / outgoing message is converted.

## How to use

Wrap your **_MessageConverter_** instance using an instance of **_TrailMetricsJmsMessageConverterWrapper_** and add the wrapper to your **_JmsTemplate_** (outgoing) and/or **_AbstractJmsListenerContainerFactory_** (incoming) implementation.