# trail-metrics

Trail Metrics offer a highly flexible way to gather measurements occurring along the execution of a process.

```xml
<dependency>
    <groupId>com.mantledillusion.metrics</groupId>
    <artifactId>trail-metrics-support</artifactId>
</dependency>
```

Get the newest version at  [mvnrepository.com/trail-metrics](https://mvnrepository.com/artifact/com.mantledillusion.metrics/trail-metrics-support)

## Introduction

When a function of a system is called, it is instructed to fulfil its use case by performing all operations necessary for that use case. 

Upon finishing, the only information about the course of the process behind the function is the result returned by the function itself. All other information about that process is not of any interest for the caller, but might be highly valuable for business intelligence, administration or debugging purposes.

For example, the caller of the REST method _addToCart(CardId cartId, ItemId itemId)_ does not need any information returned; the item just needs to be added to the shopping cart. But administration might want a level of response times for several sub functions called to monitor the systems latency when performing the operation. This scenario would just require plain metrics; a contextless, overall metric for the whole system would be enough.

But what if a certain customer files a complaint, that adding an item to his cart takes extremely long and sometimes even completely fails with a time out? Since a plain metric would be an average value, a single spike in latency of one sub-operation would not affect the respective monitored metric notably. And the logs of the method's application would just contain the server side message, that a client has unexpectedly ended a connection; since the operation was just long taking and caused no error on server side, it would be cumbersome to find out what went wrong.

That's where the trail comes in. The trail identifies an arbitrary set of coherent operations as a single process. So when metrics are written along that process, they become the **_MetricsTrail_**

In the example of the customer adding an item to the cart, all of the customers actions since he or she logged in might be done in the same trail. That way, when the timeout appears in the front end, an error report containing the trail's ID can be created. Using that ID, all metrics during that trail can be analyzed individually, revealing a spike in latency in a specific sub-operation when that specific user adds items to his cart.

## Committing and Consuming Measurements

### What is an Event?

The **_Event_** class describes a specific type of incidence happening at a specific point in time.

It is a simple POJO, consisting of:
- A freely selectable **identifier**, that will characterize the measurements in the context they are committed in
- A **timestamp**, which is auto-created
- A list of its **measurements**

### What is a Measurement?

The **_Measurement_** class is used to describe meta data to an **_Event_** and consists of:
- A key, which is unique for its event
- A value
- A type determining the value's content, which is one of:
  - STRING
  - BOOLEAN
  - SHORT
  - INTEGER
  - LONG
  - FLOAT
  - DOUBLE
  - BIGINTEGER
  - BIGDECIMAL
  - LOCAL_DATE
  - LOCAL_TIME
  - LOCAL_DATETIME
  - ZONED_DATETIME

### How do I write an Event and its Measurements to a Trail?

First, a trail needs to be open on the current thread, which can be done by calling  _**MetricsTrailSupport**.begin()_, while _**MetricsTrailSupport**.end()_ will close the **_MetricsTrail_** again, flushing all queued up metrics.

In most cases, this has not to be done manually, but is rather done by a specialized **_support_** package, which will auto-open and close trails on incoming HTTP requests, in UI sessions, on cron task executions or when ever.

As long as a trail is hooked to the current thread, the static _**MetricsTrailSupport**.commit()_ method is used for dispatching metrics to the current thread's trail:

```java
MetricsTrailSupport.commit(new Event("my.event.identifier",
        new Measurement("my.measurement.key1", "foobar", MeasurementType.STRING),
        new Measurement("my.measurement.key2", "1.337", MeasurementType.FLOAT)));
```

### How do I receive Events from Trails?

The **_MetricsConsumer_** interface is used to create consumers to trails:

```java
MetricsTrailConsumer myConsumer = MetricsTrailConsumer.from("my.consumer", (consumerId, correlationId, event) ->
        // A super simple consumer that will just print any received event to console
        System.out.println("Consumer "+consumerId+" received event of trail "+correlationId+": "+event.toString()));
```

The **_MetricsTrailConsumer_** class wraps an instance of **_MetricsConsumer_**, adding a whole of configurable standard functionality like:
- Asychronism with retry, so committing a measurement to a consumer will <u>never</u> cause an exception in a committing thread
- Gates to hold back **_Metric_** instance committing to a trail until a certain type of **_Metric_** occurs
- Filters to prevent certain types of **_Metric_** instances to be committed to trails

To ensure consumers are automatically hooked to all trails opened, a simple _**MetricsTrailSupport.hook()**_ call will hook a **_MetricsTrailConsumer_** to all thread based trails:

```java
MetricsTrailSupport.hook(myConsumer);
```

The **_adapter_** packages provide a lot of reference implementations for standard consumers of measurements, like logs, web services, databases and so on.

### I have an application I want to integrate MetricTrails into, how do i get started?

First, choose all the **_support_** packages that fit your application, or hook the basic _trail-measurements-support_ yourself. It will enable your application to create measurements in all situations that might occur. Then simply create as many measurements as required and commit them.

To consume the created measurements, refer to the **_adaptor_** packages for base implementations of consumers and choose as many as needed. Alternatively, implement the **_MetricsConsumer_** interface yourself.
