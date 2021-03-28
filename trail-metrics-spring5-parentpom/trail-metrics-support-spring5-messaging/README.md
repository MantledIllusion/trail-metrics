# trail-measurements-support-spring-messaging

Contains a Spring Messaging interceptor for automatically adding a **_MetricTrail_**'s ID as a header when sending / receiving messages.

## How to use

Add an instance of **_TrailMetricsMessagingChannelInterceptor_** to the respective channel.