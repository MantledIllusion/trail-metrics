# trail-metrics-adaptor-web

A **_MetricsConsumer_** that is able to package and send metrics via web service in JAX-B annotated POJOs.

On the client side, register a _com.mantledillusion.metrics.trail.**MetricsSender**_ instance as consumer to the used **_MetricsObserver_** implementation. The MetricsSender will require an implementation _com.mantledillusion.metrics.trail.**MetricsWebFacade**_ that is able to transfer the packaged metrics by a web service client of your choice.

On the server side, receive the packaged metrics by a web service server of your choice and hand them to an instance of _com.mantledillusion.metrics.trail.**MetricsReceiver**_. The _**MetricsReceiver**_ will unpack them and act as a _**MetricsQueue**_ that accepts _**MetricsConsumer**_ implementations to be registered on the server side.