# trail-measurements-adaptor-slf4j

A **_MetricsConsumer_** that is able to log measurements using SLF4J.

## How to use

Hook a _com.mantledillusion.measurements.trail.**MetricsLogger**_ instance as consumer to the used **_MetricsTrail_**.

The _**MetricsLogger**_ can be instantiated by calling _**MetricsLogger**.from()_, configuring the returned _com.mantledillusion.measurements.trail.**MetricsLogger.MetricsLoggerBuilder**_ and then invoking _**MetricsLoggerBuilder**.build()_.