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
 * Use {@link TrailMetricsMessagingChannelInterceptor#TrailMetricsMessagingChannelInterceptor(String)} or the
 * {@value #PRTY_HEADER_NAME} property to set the header name to use, which is {@value #DEFAULT_HEADER_NAME} by default.
 * <p>
 * If the header of an incoming message is set and contains a UUID, it will be used to begin the {@link MetricsTrail}
 * with; if not, a {@link UUID#randomUUID()} is used.
 */
public class TrailMetricsMessagingChannelInterceptor implements ChannelInterceptor {

    public static final String PRTY_HEADER_NAME = "trailMetrics.message.trailIdHeaderName";
    public static final String DEFAULT_HEADER_NAME = "trailId";

    private final String headerName;

    /**
     * Default constructor.
     * <p>
     * Uses {@link #DEFAULT_HEADER_NAME} as header name for the trail ID.
     */
    public TrailMetricsMessagingChannelInterceptor() {
        this(DEFAULT_HEADER_NAME);
    }

    /**
     * Advanced constructor.
     *
     * @param headerName The name to use as header name when transmitting a {@link MetricsTrail}'s ID; might <b>not</b> be blank.
     */
    @Autowired
    public TrailMetricsMessagingChannelInterceptor(@Value("${"+PRTY_HEADER_NAME+":"+DEFAULT_HEADER_NAME+"}") String headerName) {
        if (headerName == null || StringUtils.isEmpty(headerName.trim())) {
            throw new IllegalArgumentException("Cannot use a blank header name.");
        }
        this.headerName = headerName;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (MetricsTrailSupport.has()) {
            message.getHeaders().put(this.headerName, MetricsTrailSupport.get().toString());
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
