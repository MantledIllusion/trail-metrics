# trail-metrics-support-spring-boot-jms

Contains Spring Boot auto configurations that automatically configures message converter wrappers for in and outgoing jms messages.

## How to use

The **_TrailMetricsJmsTemplateAutoConfiguration_** will activate automatically if one or more **_JmsTemplate_** beans are existing, wrapping each template's **_MessageConverter_** in a  **_TrailMetricsJmsMessageConverterWrapper_**, which automatically adds a **_MetricTrail_**'s ID as JMS connection ID header when sending a message.

The **_TrailMetricsJmsListenerContainerFactoryAutoConfiguration_** will activate automatically if one or more **_AbstractJmsListenerContainerFactory_** beans are existing, wrapping each template's **_MessageConverter_** in a  **_TrailMetricsJmsMessageConverterWrapper_**, which automatically begins a **_MetricTrail_** as a JMS message comes in.

## Config

```yaml
trailMetrics:
  jms:
    messageConverter: <The Spring bean qualifier of the MessageConverter to wrap>
    incomingMode: <How to handle incoming JMS messages, one of [STRICT, LENIENT, OPTIONAL], LENIENT by default>
    outgoingMode: <How to handle outgoing JMS messages, one of [STRICT, LENIENT, OPTIONAL], OPTIONAL by default>
    dispatchReceive: <Whether to dispatch a metric when a message is received>
```

## Metrics dispatched

- ALERT spring.jms.message.receive: When a message is received.
  - destination: The destination the message was received on.
  - originalCorrelationId: The correlation ID of the incoming message if it could not be used.