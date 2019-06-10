# trail-metrics

A MetricsTrail is an ordered queue of metrics with asynchronous per-consumer delivery.

Each trail has a dedicated queue for its metrics on each consumer, so each consumer can define own configurations of which metrics to consume and when, without affecting other consumers.

## Configurable Consuming
A consumer might consume all of each trail's metrics bit by bit as they occur. Another consumer might wait until a specific type of metric occurs which will cause all accumulated metrics of the corresponding session to be flushed in one go. Or a consumer might only want to consume metrics of a certain make. For this functionality, each consumer might define its own gate and filter if desired.

Such configuration is done by specifying _**MetricPredicates**_ which, equal to the Java Util Predicates, can be AND and OR conjuncted.

### Gates
Gates are able to suspend delivering metrics of this queue to the consumer until a metric is enqueued that opens the gate.

A gate _**MetricPredicate**_ is stateless, so when the gate has opened because of a specific metric and all accumulated events have been flushed to be delivered to the consumer, it closes again until a second special event opens it again. If the predicate is turned into a stateful **_MetricValve_**, the gate will stay open after it has been opened once.

### Filters
Filters are able to sort-out metrics that passed the gate and are ready to be delivered to the consumer.

A filter _**MetricPredicate**_ is stateless, so when the filter lets a metric pass, the next metric will be checked individually, probably causing it to be dropped if it does not match the filter's criteria. If the predicate is turned into a stateful _**MetricValve**_, the filter will not drop metrics any more after one metric has passed it that fulfilled its criteria.
