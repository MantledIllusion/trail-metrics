package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * {@link ChannelInterceptor} that will add the calling {@link Thread} {@link MetricsTrail}'s ID as a header to any
 * outgoing message and will begin a {@link MetricsTrail} on the {@link Thread} of an incoming message that will end
 * when the message has been received.
 * <p>
 * Use {@link TrailMetricsMessagingChannelInterceptor#TrailMetricsMessagingChannelInterceptor(String, String, String)}
 * or the {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by
 * default.
 * <p>
 * Use {@link TrailMetricsMessagingChannelInterceptor#TrailMetricsMessagingChannelInterceptor(String, String, String)}
 * or the {@value #PRTY_INCOMING_MODE}/{@value #PRTY_OUTGOING_MODE} properties to set the modes to use, which are
 * {@value #DEFAULT_INCOMING_MODE}/{@value #DEFAULT_OUTGOING_MODE} by default.
 * <p>
 * If the header of an incoming message is set and contains a UUID, it will be used to begin the {@link MetricsTrail}
 * with; if not, a {@link UUID#randomUUID()} is used.
 */
public class TrailMetricsMessagingChannelInterceptor implements ChannelInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.message.trailIdHeaderName";
    public static final String PRTY_INCOMING_MODE = "trailMetrics.http.incomingMode";
    public static final String PRTY_OUTGOING_MODE = "trailMetrics.http.outgoingMode";
    public static final String DEFAULT_HEADER_NAME = "trailId";
    public static final String DEFAULT_INCOMING_MODE = "LENIENT";
    public static final String DEFAULT_OUTGOING_MODE = "OPTIONAL";

    private final String headerName;
    private TrailBehaviourMode incomingMode;
    private TrailBehaviourMode outgoingMode;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsMessagingChannelInterceptor() {
        this(DEFAULT_HEADER_NAME, TrailBehaviourMode.LENIENT, TrailBehaviourMode.OPTIONAL);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param outgoingMode The behaviour mode for outgoing headers; might <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsMessagingChannelInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName,
                                                   @Value("${"+ PRTY_INCOMING_MODE +":"+ DEFAULT_INCOMING_MODE +"}") String incomingMode,
                                                   @Value("${"+PRTY_OUTGOING_MODE+":"+DEFAULT_OUTGOING_MODE+"}") String outgoingMode) {
        this(headerName, TrailBehaviourMode.valueOf(incomingMode), TrailBehaviourMode.valueOf(outgoingMode));
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     * @param incomingMode The behaviour mode for incoming headers; might <b>not</b> be null.
     * @param outgoingMode The behaviour mode for outgoing headers; might <b>not</b> be null.
     */
    @Autowired
    public TrailMetricsMessagingChannelInterceptor(String headerName,
                                                   TrailBehaviourMode incomingMode, TrailBehaviourMode outgoingMode) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
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
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (MetricsTrailSupport.has()) {
            message.getHeaders().put(this.headerName, MetricsTrailSupport.get().toString());
        } else {
            switch (this.outgoingMode) {
                case STRICT:
                    throw new IllegalStateException();
                case LENIENT:
                    message.getHeaders().put(this.headerName, UUID.randomUUID().toString());
            }
        }
        return message;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        UUID trailId;
        try {
            trailId = UUID.fromString(Objects.toString(message.getHeaders().get(this.headerName)));
        } catch (NullPointerException | IllegalArgumentException e) {
            trailId = UUID.randomUUID();
        }
        MetricsTrailSupport.begin(trailId);
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }
}
