# trail-measurements-support-vaadin-14

A _**MetricsTrail**_ support for Vaadin Flow 14.

## How to use

Using _**VaadinMetricsTrailSupport**.support()_ on a **_VaadinService_**, every session of that service instance will automatically receive its own **_MetricsTrail_**.

By calling _**VaadinMetricsTrailSupport**.getCurrent()_ that trail can be accessed statically from any thread in the context of a _**VaadinSession**_.

### Default Vaadin Metrics

By supporting a **_VaadinService_**, default measurements will be written for:
- The begin of a session
- The info about the browser used
- The end of a session
- The URL navigation done in the application

By supporting a **_ErrorHandler_**, default measurements will be written for:
- Every uncaught error of the application