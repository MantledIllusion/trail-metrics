package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

abstract class AbstractMetricsWrappingErrorHandler<H> {

    public static final String ATTRIBUTE_KEY_TYPE = "type";
    public static final String ATTRIBUTE_KEY_MESSAGE = "message";
    public static final String ATTRIBUTE_KEY_STACKTRACE = "stackTrace";

    private final H wrappedErrorHandler;
    private final Supplier<MetricsTrail> trailSupplier;

    AbstractMetricsWrappingErrorHandler(H wrappedErrorHandler, Supplier<MetricsTrail> trailSupplier) {
        this.wrappedErrorHandler = wrappedErrorHandler;
        this.trailSupplier = trailSupplier;
    }

    protected void commit(Throwable t) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        t.printStackTrace(writer);

        Metric metric = GeneralVaadinMetrics.ERROR.build(t.getClass().getSimpleName());
        metric.getAttributes().add(new MetricAttribute(ATTRIBUTE_KEY_TYPE, t.getClass().getName()));
        metric.getAttributes().add(new MetricAttribute(ATTRIBUTE_KEY_MESSAGE, t.getMessage()));
        metric.getAttributes().add(new MetricAttribute(ATTRIBUTE_KEY_STACKTRACE, out.toString()));

        this.trailSupplier.get().commit(metric);
    }

    /**
     * Returns the error handler wrapped by this wrapping handler.
     *
     * @return The wrapped error handler, may be null
     */
    public H getWrappedErrorHandler() {
        return this.wrappedErrorHandler;
    }
}
