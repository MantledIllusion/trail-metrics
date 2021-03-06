# trail-metrics

Trail Metrics offer a highly flexible way to gather metrics occurring along the execution of a process.

## Introduction

When a function of a system is called, it is instructed to fulfil its use case by performing all operations necessary for that use case. 

Upon finishing, the only information about the course of the process behind the function is the result returned by the function itself. All other information about that process is not of any interest for the caller, but might be highly valuable for business intelligence, administration or debugging purposes.

For example, the caller of the REST method _addToCart(CardId cartId, ItemId itemId)_ does not need any information returned; the item just needs to be added to the shopping cart. But administration might want a level of response times for several sub functions called to monitor the systems latency when performing the operation. This scenario would just require plain metrics; a contextless, overall metric for the whole system would be enough.

But what if a certain customer files a complaint, that adding an item to his cart takes extremely long and sometimes even completely fails with a time out? Since a plain metric would be an average value, a single spike in latency of one sub-operation would not affect the respective monitored metric notably. And the logs of the method's application would just contain the server side message, that a client has unexpectedly ended a connection; since the operation was just long taking and caused no error on server side, it would be cumbersome to find out what went wrong.

That's where the trail comes in. The trail identifies an arbitrary set of coherent operations as a single process. So when metrics are written along that process, they become the **_MetricsTrail_**

In the example of the customer adding an item to the cart, all of the customers actions since he or she logged in might be done in the same trail. That way, when the timeout appears in the front end, an error report containing the trail's ID can be created. Using that ID, all metrics during that trail can be analyzed individually, revealing a spike in latency in a specific sub-operation when that specific user adds items to his cart.

## Committing and Consuming Metrics

### What is a Metric?

The **_Metric_** class is a simple POJO, consisting of:
- A freely selectable **identifier**, that will characterize the metric in the context it is committed in
- A **type**, which is one of:
  - ALERT: a metric that simply alters the occurrence of a specific event, for example a user trying to authenticate with wrong credentials
  - PHASE: a metric that defines the switch in phase, for example the transitions of a shopping cart from gathering products, to requesting an offer with combined prices from the seller, to a combined price being offered to customer
  - METER: a metric that notifies about the exact amount a certain value has, for example the milliseconds a backend call required
  - TREND: a metric that notifies about the change in amount a certain value underwent, for examle a -1/+1 every time a user found a help article useful
- A **timestamp**, which is auto-created
- A key->value map of **attributes**

### How do I write Metrics to a MetricsTrail?

The _**MetricsTrail**.commit()_ method accepts instances of the **_Metric_** class. 

In order to have a **_MetricsTrail_** to write to anywhere in your application, use a **_support_** package that will begin **_MetricsTrail_** instances automatically, for example on incoming HTTP requests, in UI sessions, on cron task executiomns or where ever.

Most of these **_support_** packages use the **_ThreadLocal_** based approach of **_MetricsTrailSupport_** class, so writing metrics to a **_MetricsTrail_** becomes as easy as calling _**MetricsTrailSupport**.commit()_ with a **_Metric_** instance as the parameter.

In case there is no suitable specialized **_support_** package, the **_MetricsTrailSupport_** can often be used as a base for custom implementations, as it only requires calls to _**MetricsTrailSupport**.begin()_ and _**MetricsTrailSupport**.end()_ to (un-)hook a **_MetricsTrail_** to the current thread.

### How do I receive MetricsTrails?

The **_MetricsConsumer_** interface is used to create consumers to trails. The **_MetricsTrailConsumer_** class wraps an instance of **_MetricsConsumer_**, adding a whole of configurable standard functionality like:
- Asychronism with retry, so committing a metric to a consumer will <u>never</u> cause an exception in a committing thread
- Gates to hold back **_Metric_** instance committing to a trail until a certain type of **_Metric_** occurs
- Filters to prevent certain types of **_Metric_** instances to be committed to trails

**_MetricsTrailConsumer_** instances can be hooked directly to a **_MetricsTrail_** instance.

When using a **_support_** package, the consumer is often given to some kind of registry though which will ensure that the consumer is automatically hooked to all trails that are created. In case of **_MetricsTrailSupport_** for example, a simple _**MetricsTrailSupport.hook()**_ call will hook a **_MetricsTrailConsumer_** to all thread based trails.

The **_adapter_** packages provide a lot of reference implementations for standard consumers of metrics, like logs, web services, databases and so on.

### I have an application i want to integrate MetricTrails into, how do i get started?

First, choose all the **_support_** packages that fit your application, or hook the basic _trail-metrics-support_ yourself. It will enable your application to create metrics in all situations that might occur. Then simply create as many metrics as required and commit them.

To consume the created metrics, refer to the **_adaptor_** packages for base implementations of consumers and choose as many as needed. Alternatively, implement the **_MetricsConsumer_** interface yourself.
