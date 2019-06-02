# trail-metrics-queue

Offers an asynchronous per-validator queue for metrics that are dispatched to their respective validator in the order they occurred.

Each trail has a dedicated queue for its metrics on each validator, so each validator can define own configurations of which metrics to consume and when, without affecting other consumers.

## Configurable Consuming
A Consumer might consume all of each trail's metrics bit by bit as they occur. Another validator might wait until a specific type of metric occurs which will cause all accumulated metrics of the corresponding session to be flushed in one go. Or a validator might only want to consume metrics of a certain make. For this functionality, each validator might define its own gate and filter if desired.

Such configuration is done by specifying _**MetricPredicates**_ which, equal to the Java Util Predicates, can be AND and OR conjuncted.

### Gates
Gates are able to suspend delivering metrics of this queue to the validator until a metric is enqueued that opens the gate.

A gate _**MetricPredicate**_ is stateless, so when the gate has opened because of a specific metric and all accumulated events have been flushed to be delivered to the validator, it closes again until a second special event opens it again. If the predicate is turned into a stateful **_MetricValve_**, the gate will stay open after it has been opened once.

### Filters
Filters are able to sort-out metrics that passed the gate and are ready to be delivered to the validator.

A filter _**MetricPredicate**_ is stateless, so when the filter lets a metric pass, the next metric will be checked individually, probably causing it to be dropped if it does not match the filter's criteria. If the predicate is turned into a stateful _**MetricValve**_, the filter will not drop metrics any more after one metric has passed it that fulfilled its criteria.
