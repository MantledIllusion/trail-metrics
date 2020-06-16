package com.mantledillusion.metrics.trail;

import org.springframework.jms.support.converter.MessageConversionException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.UUID;

/**
 * {@link MessageListener} that will begin a {@link MetricsTrail} on the {@link Thread} of an incoming message with the
 * ID set as JMS correlation ID.
 * <p>
 * The behaviour regarding missing {@link MetricsTrail}s/JMS correlation IDs on incoming messages can be modified
 * using {@link #setIncomingMode(TrailBehaviourMode)}.
 *
 * @see Message#getJMSCorrelationID()
 */
public class TrailMetricsJmsListenerWrapper implements MessageListener {

    private final MessageListener wrappedListener;
    private TrailBehaviourMode incomingMode;

    /**
     * Default constructor.
     * <p>
     * Sets the mode for incoming messages to {@link TrailBehaviourMode#LENIENT}.
     *
     * @param wrappedListener The {@link MessageListener} to wrap; might <b>not</b> be null.
     */
    public TrailMetricsJmsListenerWrapper(MessageListener wrappedListener) {
        this(wrappedListener, TrailBehaviourMode.LENIENT);
    }

    /**
     * Advanced constructor.
     *
     * @param wrappedListener The {@link MessageListener} to wrap; might <b>not</b> be null.
     * @param incomingMode The behaviour mode for incoming messages; might <b>not</b> be null.
     */
    public TrailMetricsJmsListenerWrapper(MessageListener wrappedListener, TrailBehaviourMode incomingMode) {
        if (wrappedListener == null) {
            throw new IllegalArgumentException("Cannot wrap a null listener");
        }
        this.wrappedListener = wrappedListener;
        setIncomingMode(incomingMode);
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

    @Override
    public void onMessage(Message message) {
        String jmsCorrelationId;
        try {
            jmsCorrelationId = message.getJMSCorrelationID();
        } catch (JMSException e) {
            jmsCorrelationId = null;
        }

        if (jmsCorrelationId == null) {
            switch (this.incomingMode) {
                case STRICT:
                    throw new MessageConversionException("Incoming JMS message does not contain correlationId.");
                case LENIENT:
                    MetricsTrailSupport.begin();
            }
        } else {
            try {
                MetricsTrailSupport.begin(UUID.fromString(jmsCorrelationId));
            } catch (IllegalArgumentException e) {
                switch (this.incomingMode) {
                    case STRICT:
                        throw new MessageConversionException("Incoming JMS message contains a non-UUID correlationId");
                    case LENIENT:
                        MetricsTrailSupport.begin();
                }
            }
        }

        try {
            this.wrappedListener.onMessage(message);
        } finally {
            if (MetricsTrailSupport.has()) {
                MetricsTrailSupport.end();
            }
        }
    }
}
