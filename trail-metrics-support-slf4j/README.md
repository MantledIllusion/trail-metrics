# trail-metrics-support-slf4j

A _**MetricsTrail**_ support for SLF4J.

## How to use

Simply call _**Slf4JMetricsTrailSupport**.activatePublishToMdc()_ when your application starts up.

The support will hook onto the thread-based **_MetricsTrailSupport_** as a listener, causing ever **_MetricTrail_**'s ID being added to SLF4J's MDC with the key "trailId".