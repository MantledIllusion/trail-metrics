# trail-metrics-support-spring-boot-jms

Contains a Spring Boot auto configurations that automatically configures message converter wrappers for in and outgoing jms messages.

## How to use

The **_TrailMetricsJmsTemplateAutoConfiguration_** will activate automatically if one or more **_JmsTemplate_** beans are existing, wrapping each template's **_MessageConverter_** in a  **_TrailMetricsJmsMessageConverterWrapper_**, which automatically adds a **_MetricTrail_**'s ID as JMS connection ID header when sending a message.

The **_TrailMetricsJmsListenerContainerFactoryAutoConfiguration_** will activate automatically if one or more **_AbstractJmsListenerContainerFactory_** beans are existing, wrapping each template's **_MessageConverter_** in a  **_TrailMetricsJmsMessageConverterWrapper_**, which automatically begins a **_MetricTrail_** as a JMS message comes in.