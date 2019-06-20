package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.Metric;

/**
 * Enum for all {@link Metric} types that are thrown by the metrics system itself.
 * <p>
 * The {@link Metric#getIdentifier()} of each entry is always the
 * {@link Enum#name()} in lower case with '_' replaced to '.' and prefixed by
 * {@value #METRICS_DOMAIN}.
 * <p>
 * For example the name of {@link #SESSION_BEGIN} is 'general.session.begin'.
 */
public enum GeneralVaadinMetrics implements EnumeratedMetric {

    /**
     * {@link Metric} ID for the {@link MetricType#ALERT} an observer creates when a new session is started.
     */
    SESSION_BEGIN(MetricType.ALERT),

    /**
     * {@link Metric} ID for the {@link MetricType#ALERT} an observer creates when a session ends.
     */
    SESSION_END(MetricType.ALERT),

    /**
     * {@link Metric} ID for the {@link MetricType#ALERT} an observer creates about the browser beginning a session.
     * <p>
     * Contains the attributes:<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_APPLICATION}: The application name<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_TYPE}: The {@link BrowserInfo.BrowserType}<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_VERSION}: The browser's version<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_ENVIRONMENT}: The {@link BrowserInfo.SystemEnvironmentType}<br>
     */
    BROWSER_INFO(MetricType.ALERT),

    /**
     * {@link Metric} ID for the {@link MetricType#PHASE} an observer creates when the URL changes.
     * <p>
     * Contains the attributes:<br>
     * - {@link Metric#OPERATOR_ATTRIBUTE_KEY}: The path navigated to<br>
     * - [query parameter key] : Query parameter values, comma separated<br>
     */
    NAVIGATION(MetricType.PHASE),

    /**
     * {@link Metric} ID for the {@link MetricType#ALERT} an {@link AbstractMetricsWrappingErrorHandler} creates when an uncatched {@link Throwable} occurs.
     * <p>
     * Contains the attributes:<br>
     * - {@link Metric#OPERATOR_ATTRIBUTE_KEY}: The simple name of the {@link Throwable}'s class<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_TYPE} : The fully qualified class name of the {@link Throwable}'s class<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_MESSAGE} : The {@link Throwable}'s message<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_STACKTRACE} : The {@link Throwable}'s stack trace<br>
     */
    ERROR(MetricType.ALERT);

    private static final String METRICS_DOMAIN = "general";

    private final String metricId;
    private final MetricType type;

    GeneralVaadinMetrics(MetricType type) {
        this.metricId = generateMetricId(METRICS_DOMAIN, this);
        this.type = type;
    }

    @Override
    public String getMetricId() {
        return metricId;
    }

    @Override
    public MetricType getType() {
        return type;
    }
}
