# trail-measurements-support-spring-jms

Contains a wrapper for Spring JMS message converters for automatically adding a **_MetricTrail_**'s ID as the JMS correlation ID header when an incoming / outgoing message is converted.

## How to use

For outgoing messages, wrap your **_MessageConverter_** instance using an instance of **_TrailMetricsJmsMessageConverterWrapper_** and add the wrapper to your **_JmsTemplate_** to add the trail ID to outgoing messages.

For incoming messages...:
- ...on _@JmsListener_ annotated methods, add your wrapped **_MessageConverter_** to your **_AbstractJmsListenerContainerFactory_** implementation and add a **_TrailMetricsJmsInterceptor_** instance as a bean to your Spring context...
- ...on custom **_JmsListener_** implementations, wrap your implementation into a **_TrailMetricsListenerWrapper_** instance...
...to extract the trail ID from an incoming message and automatically start / end a trail.

## Config

```yaml
trailMetrics:
  jms:
    messageConverter: <The Spring bean qualifier of the MessageConverter to wrap>
    incomingMode: <How to handle incoming JMS messages, one of [STRICT, LENIENT, OPTIONAL], LENIENT by default>
    outgoingMode: <How to handle outgoing JMS messages, one of [STRICT, LENIENT, OPTIONAL], OPTIONAL by default>
    dispatchReceive: <Whether to dispatch a measurement when a message is received>
```

## Metrics dispatched

- ALERT spring.jms.message.receive: When a message is received.
  - destination: The destination the message was received on.
  - originalCorrelationId: The correlation ID of the incoming message if it could not be used.