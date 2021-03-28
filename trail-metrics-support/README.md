# trail-measurements-support

Universal thread-based _**MetricsTrail**_ support.

## How to use

Simply call _**MetricsTrailSupport**.begin()_ to begin a **_MetricsTrail_** for the current thread.

Using _**MetricsTrailSupport**.hook()_, a **_MetricsTrailConsumer_** can be hooked to the thread's **_MetricsTrail_** to receive all of its _**Metrics**_.

Afterwards, _**MetricsTrailSupport**.commit()_ can be used on that thread to dispatch a **_Metric_** to all consumers that are hooked to it.

Finally _**MetricsTrailSupport**.end()_ will end the trail for the thread, triggering a final _**Metric**_ consuming (if desired) and un-hooking all consumers.