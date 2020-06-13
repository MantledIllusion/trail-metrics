package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Value;
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
 * modified using {@link #setIncomingMode(TrailBehaviourMode)} / {@link #setOutgoingMode(TrailBehaviourMode)}.
 *
 * @see Message#getJMSCorrelationID()
 * @see Message#setJMSCorrelationID(String)
 */
public class TrailMetricsJmsMessageConverterWrapper implements MessageConverter {

    public static final String PRTY_MESSAGE_CONVERTER = "trailMetrics.jms.messageConverter";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.jms.incomingMode";
    public static final String PRTY_OUTGOING_MODE = "trailMetrics.jms.outgoingMode";
    public static final String DEFAULT_MESSAGE_CONVERTER = "messageConverter";
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";
    public static final String DEFAULT_OUTGOING_MODE = "OPTIONAL";

    private final MessageConverter wrappedConverter;
    private TrailBehaviourMode incomingMode;
    private TrailBehaviourMode outgoingMode;

    /**
     * Default constructor.
     * <p>
     * Sets the mode for incoming messages to {@link TrailBehaviourMode#LENIENT} and the one for outgoing messages to
     * {@link TrailBehaviourMode#OPTIONAL}.
     *
     * @param wrappedConverter The {@link MessageConverter} to wrap; might <b>not</b> be null.
     */
    public TrailMetricsJmsMessageConverterWrapper(MessageConverter wrappedConverter) {
        this(wrappedConverter, TrailBehaviourMode.LENIENT, TrailBehaviourMode.OPTIONAL);
    }

    /**
     * Default constructor.
     * <p>
     * Sets the mode for incoming messages to {@link TrailBehaviourMode#LENIENT} and the one for outgoing messages to
     * {@link TrailBehaviourMode#OPTIONAL}.
     *
     * @param wrappedConverter The {@link MessageConverter} to wrap; might <b>not</b> be null.
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     * @param outgoingMode The behaviour mode for outgoing messages; might <b>not</b> be null.
     */
    public TrailMetricsJmsMessageConverterWrapper(@Value("${"+ PRTY_MESSAGE_CONVERTER +":"+ DEFAULT_MESSAGE_CONVERTER +"}") MessageConverter wrappedConverter,
                                                  @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode,
                                                  @Value("${"+PRTY_OUTGOING_MODE+":"+DEFAULT_OUTGOING_MODE+"}") String outgoingMode) {
        this(wrappedConverter, TrailBehaviourMode.valueOf(incomingMode), TrailBehaviourMode.valueOf(outgoingMode));
    }

    /**
     * Advanced constructor.
     *
     * @param wrappedConverter The {@link MessageConverter} to wrap; might <b>not</b> be null.
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     * @param outgoingMode The behaviour mode for outgoing messages; might <b>not</b> be null.
     */
    public TrailMetricsJmsMessageConverterWrapper(MessageConverter wrappedConverter,
                                                  TrailBehaviourMode incomingMode, TrailBehaviourMode outgoingMode) {
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
    public TrailBehaviourMode getIncomingMode() {
        return incomingMode;
    }

    /**
     * Sets the used mode for incoming messages.
     *
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     */
    public void setIncomingMode(TrailBehaviourMode incomingMode) {
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
    public TrailBehaviourMode getOutgoingMode() {
        return outgoingMode;
    }

    /**
     * Sets the used mode for outgoing messages.
     *
     * @param outgoingMode The behaviour mode for outgoing messages; might <b>not</b> be null.
     */
    public void setOutgoingMode(TrailBehaviourMode outgoingMode) {
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
