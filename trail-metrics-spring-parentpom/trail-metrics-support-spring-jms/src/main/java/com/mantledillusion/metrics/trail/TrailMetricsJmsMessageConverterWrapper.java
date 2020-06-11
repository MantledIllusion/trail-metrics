package com.mantledillusion.metrics.trail;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.UUID;

/**
 * {@link MessageConverter} that will add the calling {@link Thread} {@link MetricsTrail}'s ID as JMS correlation ID
 * header to any outgoing message and will begin a {@link MetricsTrail} on the {@link Thread} of an incoming message
 * with the ID set as JMS correlation ID.
 * <p>
 * The behaviour regarding missing {@link MetricsTrail}s/JMS correlation IDs on incoming/outgoing messages can be
 * modified using {@link #setIncomingMode(JmsTrailMode)} / {@link #setOutgoingMode(JmsTrailMode)}.
 *
 * @see Message#getJMSCorrelationID()
 * @see Message#setJMSCorrelationID(String)
 */
public class TrailMetricsJmsMessageConverterWrapper implements MessageConverter {

    private final MessageConverter wrappedConverter;
    private JmsTrailMode incomingMode;
    private JmsTrailMode outgoingMode;

    /**
     * Default constructor.
     * <p>
     * Sets the mode for incoming messages to {@link JmsTrailMode#LENIENT} and the one for outgoing messages to
     * {@link JmsTrailMode#OPTIONAL}.
     *
     * @param wrappedConverter The {@link MessageConverter} to wrap; might <b>not</b> be null.
     */
    public TrailMetricsJmsMessageConverterWrapper(MessageConverter wrappedConverter) {
        this(wrappedConverter, JmsTrailMode.LENIENT, JmsTrailMode.OPTIONAL);
    }

    /**
     * Advanced constructor.
     *
     * @param wrappedConverter The {@link MessageConverter} to wrap; might <b>not</b> be null.
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     * @param outgoingMode The behaviour mode for outgoing messages; might <b>not</b> be null.
     */
    public TrailMetricsJmsMessageConverterWrapper(MessageConverter wrappedConverter,
                                                  JmsTrailMode incomingMode, JmsTrailMode outgoingMode) {
        if (wrappedConverter == null) {
            throw new IllegalArgumentException("Cannot wrap a null converter");
        }
        this.wrappedConverter = wrappedConverter;
        setIncomingMode(incomingMode);
        setOutgoingMode(outgoingMode);
    }

    /**
     * Returns the currently used mode for incoming messages.
     *
     * @return The current mode, never null
     */
    public JmsTrailMode getIncomingMode() {
        return incomingMode;
    }

    /**
     * Sets the used mode for incoming messages.
     *
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     */
    public void setIncomingMode(JmsTrailMode incomingMode) {
        if (incomingMode == null) {
            throw new IllegalArgumentException("Cannot set a null mode");
        }
        this.incomingMode = incomingMode;
    }

    /**
     * Returns the currently used mode for outgoing messages.
     *
     * @return The current mode, never null
     */
    public JmsTrailMode getOutgoingMode() {
        return outgoingMode;
    }

    /**
     * Sets the used mode for outgoing messages.
     *
     * @param outgoingMode The behaviour mode for outgoing messages; might <b>not</b> be null.
     */
    public void setOutgoingMode(JmsTrailMode outgoingMode) {
        if (outgoingMode == null) {
            throw new IllegalArgumentException("Cannot set a null mode");
        }
        this.outgoingMode = outgoingMode;
    }

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        if (message.getJMSCorrelationID() == null) {
            switch (this.incomingMode) {
                case STRICT:
                    throw new MessageConversionException("");
                case LENIENT:
                    MetricsTrailSupport.begin();
            }
        } else {
            try {
                MetricsTrailSupport.begin(UUID.fromString(message.getJMSCorrelationID()));
            } catch (IllegalArgumentException e) {
                switch (this.incomingMode) {
                    case STRICT:
                        throw new MessageConversionException("");
                    case LENIENT:
                        MetricsTrailSupport.begin();
                }
            }
        }
        return this.wrappedConverter.fromMessage(message);
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        Message message = this.wrappedConverter.toMessage(object, session);
        if (MetricsTrailSupport.has()) {
            message.setJMSCorrelationID(MetricsTrailSupport.get().toString());
        } else {
            switch (this.outgoingMode) {
                case STRICT:
                    throw new MessageConversionException("");
                case LENIENT:
                    message.setJMSCorrelationID(UUID.randomUUID().toString());
            }
        }
        return message;
    }
}
