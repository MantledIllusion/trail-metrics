# trail-metrics-adaptor-web

A **_MetricsConsumer_** that is able to package and send metrics via web service in JAX-B annotated POJOs.

## How to use (Client Side)
On the client side, hook a _com.mantledillusion.metrics.trail.**MetricsSender**_ instance as consumer to the used **_MetricsTrail_**. 

The **_MetricsSender_** will require an implementation _com.mantledillusion.metrics.trail.**MetricsWebFacade**_ that is able to transfer the packaged metrics by a web service client of your choice.


## How to use (Server Side)
On the server side, receive the packaged metrics by a web service endpoint of your choice and hand them to an instance of _com.mantledillusion.metrics.trail.**MetricsReceiver**_. 

The _**MetricsReceiver**_ will unpack them and deliver them to _**MetricsConsumer**_ implementations hooked to it.