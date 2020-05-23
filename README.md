# trail-metrics

Trail Metrics offer a highly flexible way to gather metrics occurring along the execution of a process.

## What is a MetricsTrail?

When a function of a system is called, it is instructed to fulfil its use case by performing all operations necessary for that use case. 

Upon finishing, the only information about the course of the process behind the function is the result returned by the function itself. All other information about that process is not of any interest for the caller, but might be highly valuable for business intelligence, administration or debugging purposes.

For example, the caller of the REST method _addToCart(CardId cartId, ItemId itemId)_ does not need any information returned; the item just needs to be added to the shopping cart. But administration might want a level of response times for several sub functions called to monitor the systems latency when performing the operation. This scenario would just require plain metrics; a contextless, overall metric for the whole system would be enough.

But what if a certain customer files a complaint, that adding an item to his cart takes extremely long and sometimes even completely fails with a time out? Since a plain metric would be an average value, a single spike in latency of one sub-operation would not affect the respective monitored metric notably. And the logs of the method's application would just contain the server side message, that a client has unexpectedly ended a connection; since the operation was just long taking and caused no error on server side, it would be cumbersome to find out what went wrong.

That's where the trail comes in. The trail identifies an arbitrary set of coherent operations as a single process. So when metrics are written along that process, they become the MetricsTrail.

In the example of the customer adding an item to the cart, all of the customers actions since he or she logged in might be done in the same trail. That way, when the timeout appears in the front end, an error report containing the trail's ID can be created. Using that ID, all metrics during that trail can be analyzed individually, revealing a spike in latency in a specific sub-operation when that specific user adds items to his cart.

## How do I get started?

To get started, simply choose the **_support_** that fits your application. It will enable your application to create metrics and to setup consumers that consume them.

Refer to the **_adaptor_** packages for base implementations of such metric consumers.