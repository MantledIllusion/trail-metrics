package com.mantledillusion.metrics.trail;

import org.springframework.jms.support.converter.MessageConversionException;

/**
 * Determines modes how to handle {@link MetricsTrail}s on incoming / outgoing messages.
 */
public enum JmsTrailMode {

    /**
     * - Incoming: throw a {@link MessageConversionException} if the message does not contain a parsable
     * {@link java.util.UUID} in the JMS correlation ID header.<br>
     * - Outgoing: throw a {@link MessageConversionException} when the current {@link Thread} does not have a
     * {@link MetricsTrail}.
     */
    STRICT,

    /**
     * - Incoming: Start a {@link MetricsTrail} with a random {@link java.util.UUID} if the message does not contain
     * a parsable {@link java.util.UUID} in the JMS correlation ID header.<br>
     * - Outgoing: Set a random {@link java.util.UUID} to the JMS correlation ID header when the current {@link Thread}
     * does not have a {@link MetricsTrail}.
     */
    LENIENT,

    /**
     * - Incoming: Do not start a {@link MetricsTrail} if the message does not contain a parsable
     * {@link java.util.UUID} in the JMS correlation ID header.<br>
     * - Outgoing: Leave the JMS correlation ID header empty when the current {@link Thread} does not have a
     * {@link MetricsTrail}.
     */
    OPTIONAL
}
