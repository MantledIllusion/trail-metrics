package com.mantledillusion.metrics.trail;

import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;

import java.util.function.Supplier;

/**
 * {@link ErrorHandler} that delegates all errors to the wrapped error handler after dispatching a
 * {@link GeneralVaadinMetrics#ERROR} metric for them.
 */
public final class MetricsWrappingErrorHandler extends AbstractMetricsWrappingErrorHandler<ErrorHandler> implements ErrorHandler {

    MetricsWrappingErrorHandler(ErrorHandler wrappedErrorHandler, Supplier<MetricsTrail> trailSupplier) {
        super(wrappedErrorHandler, trailSupplier);
    }

    @Override
    public void error(ErrorEvent event) {
        commit(event.getThrowable());
        getWrappedErrorHandler().error(event);
    }
}
