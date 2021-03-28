package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

/**
 * Enum for all {@link Event} types that are thrown by the metrics system itself.
 * <p>
 * The {@link Event#getIdentifier()} of each entry is always the {@link Enum#name()} in lower case with '_' replaced
 * to '.' and prefixed by {@value #METRICS_DOMAIN}.
 * <p>
 * For example the name of {@link #SESSION_BEGIN} is 'general.session.begin'.
 */
public enum GeneralVaadinMetrics implements EnumeratedEvent {

    /**
     * The {@link Event} an observer creates when a new session is started.
     * <p>
     * Contains the attributes:<br>
     * - {@link AbstractVaadinMetricsTrailSupport#ATTRIBUTE_KEY_SESSION_ID}: The session ID<br>
     */
    SESSION_BEGIN,

    /**
     * The {@link Event} an observer creates when a session ends.
     */
    SESSION_END,

    /**
     * The {@link Event} an observer creates about the browser beginning a session.
     * <p>
     * Contains the attributes:<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_APPLICATION}: The application name<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_TYPE}: The {@link BrowserInfo.BrowserType}<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_VERSION}: The browser's version<br>
     * - {@link BrowserInfo#ATTRIBUTE_KEY_ENVIRONMENT}: The {@link BrowserInfo.SystemEnvironmentType}<br>
     */
    BROWSER_INFO,

    /**
     * The {@link Event} an observer creates when the URL changes.
     * <p>
     * Contains the attributes:<br>
     * - {@link AbstractVaadinMetricsTrailSupport#ATTRIBUTE_KEY_PATH}: The path navigated to<br>
     * - {@link AbstractVaadinMetricsTrailSupport#ATTRIBUTE_KEY_PARAM_PREFIX}[query parameter key]: Query parameter values, comma separated<br>
     */
    NAVIGATION,

    /**
     * The {@link Event} an {@link AbstractMetricsWrappingErrorHandler} creates when an uncatched {@link Throwable} occurs.
     * <p>
     * Contains the attributes:<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_SIMPLE_TYPE} : The simple name of the {@link Throwable}'s class<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_TYPE} : The fully qualified class name of the {@link Throwable}'s class<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_MESSAGE} : The {@link Throwable}'s message<br>
     * - {@link AbstractMetricsWrappingErrorHandler#ATTRIBUTE_KEY_STACKTRACE} : The {@link Throwable}'s stack trace<br>
     */
    ERROR;

    public static final String METRICS_DOMAIN = "vaadin.";

    @Override
    public String getPrefix() {
        return METRICS_DOMAIN;
    }
}
