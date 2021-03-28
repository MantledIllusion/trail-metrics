package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * {@link MetricsConsumer} implementation that is able to log consumed {@link Event}s using a SLF4J {@link Logger}.
 */
public class MetricsLogger implements MetricsConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsLogger.class);
    private static final String MSG_HEAD = "Consumer '%s' (%s): '%s' at %s";
    private static final String MSG_SEPARATOR = "; ";
    private static final Map<Level, Consumer<String>> MESSAGE_DISTRIBUTORS;

    static {
        Map<Level, Consumer<String>> messageDistributors = new EnumMap<>(Level.class);
        messageDistributors.put(Level.TRACE, LOGGER::trace);
        messageDistributors.put(Level.DEBUG, LOGGER::debug);
        messageDistributors.put(Level.INFO, LOGGER::info);
        messageDistributors.put(Level.WARN, LOGGER::warn);
        messageDistributors.put(Level.ERROR, LOGGER::error);
        MESSAGE_DISTRIBUTORS = Collections.unmodifiableMap(messageDistributors);
    }

    private static final Function<Event, String> DEFAULT_MESSAGE_RENDERER = metric ->
            metric.getMeasurements() == null ? "[]" : '['+metric.getMeasurements().stream()
                    .map(attr -> attr.getKey()+'='+attr.getValue())
                    .reduce((attr1, attr2) -> attr1+", "+attr2)
                    .orElse("")+']';

    /**
     * Builder for {@link MetricsLogger}s.
     */
    public static final class MetricsLoggerBuilder {

        private Level defaultLevel = Level.INFO;
        private final Map<String, Level> levelMappings = new HashMap<>();
        private Function<ZonedDateTime, String> dateTimeRenderer = ZonedDateTime::toString;
        private Function<Event, String> defaultMessageRenderer = DEFAULT_MESSAGE_RENDERER;
        private final Map<String, Function<Event, String>> messageRenderers = new HashMap<>();

        private MetricsLoggerBuilder() {

        }

        /**
         * Sets the {@link Level} to use when there is no specific level configured on a {@link Event}'s identifier by
         * using {@link #setMetricLevel(String, Level)}.
         * <p>
         * By standard the default level is {@link Level#INFO}.
         *
         * @param defaultLevel The {@link Level}; might <b>not</b> be null.
         * @return this
         */
        public MetricsLoggerBuilder setDefaultLevel(Level defaultLevel) {
            if (defaultLevel == null) {
                throw new IllegalArgumentException("Cannot set the default level to null");
            }
            this.defaultLevel = defaultLevel;
            return this;
        }

        /**
         * Sets the {@link Level} to use for logging every {@link Event} of the given identifier.
         *
         * @param identifier The {@link Event}'s identifier to configure the given {@link Level} for; might <b>not</b>
         *                   be null.
         * @param level The {@link Level} to configure for the given {@link Event} identifier; might <b>not</b> be
         *              null.
         * @return this
         */
        public MetricsLoggerBuilder setMetricLevel(String identifier, Level level) {
            if (identifier == null) {
                throw new IllegalArgumentException("Cannot configure a log level for a null identifier");
            } else if (level == null) {
                throw new IllegalArgumentException("Cannot configure a null log level for an identifier");
            }
            this.levelMappings.put(identifier, level);
            return this;
        }

        /**
         * Sets the {@link Function} to use when rendering {@link ZonedDateTime} of {@link Event#getTimestamp()} for
         * log messages.
         * <p>
         * By standard {@link ZonedDateTime#toString()} is used.
         *
         * @param dateTimeRenderer The {@link Function} to use for rendering; might <b>not</b> be null.
         * @return this
         */
        public MetricsLoggerBuilder setDateTimeRenderer(Function<ZonedDateTime, String> dateTimeRenderer) {
            if (dateTimeRenderer == null) {
                throw new IllegalArgumentException("Cannot set a null function to use as date time renderer");
            }
            this.dateTimeRenderer = dateTimeRenderer;
            return this;
        }

        /**
         * Sets the {@link Function} to use when there is no specific renderer configured on a {@link Event}'s
         * identifier by using {@link #setMetricRenderer(String, Function)}.
         * <p>
         * By standard a {@link Function} is used that simple prints out the operator of a {@link Event} if it has one.
         *
         * @param defaultMessageRenderer The {@link Function} to use for rendering; might <b>not</b> be null.
         * @return this
         */
        public MetricsLoggerBuilder setDefaultMessageRenderer(Function<Event, String> defaultMessageRenderer) {
            if (defaultMessageRenderer == null) {
                throw new IllegalArgumentException("Cannot set a null function to use as message renderer");
            }
            this.defaultMessageRenderer = defaultMessageRenderer;
            return this;
        }

        /**
         * Sets a {@link Function} to render log messages with for {@link Event}s of the given identifier.
         *
         * @param identifier The {@link Event}'s identifier to configure the given {@link Level} for; might <b>not</b>
         *                   be null.
         * @param messageRenderer The {@link Function} to use for rendering; might <b>not</b>
         * @return this
         */
        public MetricsLoggerBuilder setMetricRenderer(String identifier, Function<Event, String> messageRenderer) {
            if (identifier == null) {
                throw new IllegalArgumentException("Cannot configure a message renderer for a null identifier");
            } else if (messageRenderer == null) {
                throw new IllegalArgumentException("Cannot configure a null message renderer for an identifier");
            }
            this.messageRenderers.put(identifier, messageRenderer);
            return this;
        }

        /**
         * Builds a new {@link MetricsLogger} with the configuration set at the time this method is invoked.
         *
         * @return A new {@link MetricsLogger} instance, never null
         */
        public MetricsLogger build() {
            return new MetricsLogger(this.defaultLevel, new HashMap<>(this.levelMappings), this.dateTimeRenderer,
                    this.defaultMessageRenderer, new HashMap<>(this.messageRenderers));
        }
    }

    private final Level defaultLevel;
    private final Map<String, Level> levelMappings;
    private final Function<ZonedDateTime, String> dateTimeRenderer;
    private final Function<Event, String> defaultMessageRenderer;
    private final Map<String, Function<Event, String>> messageRenderers;

    private MetricsLogger(Level defaultLevel, Map<String, Level> levelMappings,
                          Function<ZonedDateTime, String> dateTimeRenderer,
                          Function<Event, String> defaultMessageRenderer,
                          Map<String, Function<Event, String>> messageRenderers) {
        this.defaultLevel = defaultLevel;
        this.levelMappings = levelMappings;
        this.dateTimeRenderer = dateTimeRenderer;
        this.defaultMessageRenderer = defaultMessageRenderer;
        this.messageRenderers = messageRenderers;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Event event) throws Exception {
        StringBuilder msgBuilder = new StringBuilder(String.format(MSG_HEAD,
                consumerId,
                correlationId.toString(),
                event.getIdentifier(),
                this.dateTimeRenderer.apply(event.getTimestamp())));

        String msg = this.messageRenderers.containsKey(event.getIdentifier()) ?
                this.messageRenderers.get(event.getIdentifier()).apply(event) :
                this.defaultMessageRenderer.apply(event);

        if (msg != null && !msg.isEmpty()) {
            msgBuilder.append(MSG_SEPARATOR).append(msg);
        }

        MESSAGE_DISTRIBUTORS.get(this.levelMappings.getOrDefault(event.getIdentifier(), this.defaultLevel)).
                accept(msgBuilder.toString());
    }

    /**
     * Begins a new {@link MetricsLoggerBuilder} to build a {@link MetricsLogger}.
     *
     * @return A new {@link MetricsLoggerBuilder}, never null.
     */
    public static MetricsLoggerBuilder from() {
        return new MetricsLoggerBuilder();
    }
}
