package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

abstract class AbstractMetricsWrappingErrorHandler<H> {

    public static final String ATTRIBUTE_KEY_SIMPLE_TYPE = "simpleType";
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

        Event event = GeneralVaadinMetrics.ERROR.build(
                new Measurement(ATTRIBUTE_KEY_SIMPLE_TYPE, t.getClass().getSimpleName(), MeasurementType.STRING),
                new Measurement(ATTRIBUTE_KEY_TYPE, t.getClass().getName(), MeasurementType.STRING),
                new Measurement(ATTRIBUTE_KEY_MESSAGE, t.getMessage(), MeasurementType.STRING),
                new Measurement(ATTRIBUTE_KEY_STACKTRACE, out.toString(), MeasurementType.STRING));

        this.trailSupplier.get().commit(event);
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
