package com.mantledillusion.metrics.trail.api.jpa;

import com.mantledillusion.metrics.trail.UUIDStringAttributeConverter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 * Represents a trail of metrics consumed by a specific consumer.
 */
@Entity
@Table(name = "metric_trail", indexes = {
        @Index(name = "IDX_METRIC_TRAIL_CORRELATION_ID", columnList = "correlation_id"),
        @Index(name = "IDX_METRIC_TRAIL_CONSUMER_ID", columnList = "consumer_id"),
        @Index(name = "UIDX_METRIC_TRAIL_CORRELATION_ID_CONSUMER_ID", columnList = "correlation_id, consumer_id", unique = true)})
public class DbMetricsConsumerTrail {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", length = 36, nullable = false)
    @Convert(converter = UUIDStringAttributeConverter.class)
    private UUID correlationId;

    @Column(name = "consumer_id", length = 256, nullable = false)
    private String consumerId;

    @OneToMany(mappedBy = "trail", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<DbMetric> metrics;

    /**
     * Default constructor.
     */
    public DbMetricsConsumerTrail() {
    }

    /**
     * Constructor.
     *
     * @param correlationId The ID of the trail; might be null.
     * @param consumerId The ID of the consumer; might be null.
     */
    public DbMetricsConsumerTrail(UUID correlationId, String consumerId) {
        this.correlationId = correlationId;
        this.consumerId = consumerId;
    }

    /**
     * Returns the database ID of this trail.
     *
     * @return The ID, may be null if the trail is not persisted yet
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of this trail.
     *
     * @param id The ID to set; might be null if this a new trail rather than an existing one in need to be updated.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the ID of this trail.
     *
     * @return The ID, might be null
     */
    public UUID getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the ID of this trail.
     *
     * @param correlationId The ID to set; might be null.
     */
    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Returns the ID of this trail's consumer.
     *
     * @return The ID, might be null
     */
    public String getConsumerId() {
        return consumerId;
    }
    /**
     * Sets the ID of this trail's consumer.
     *
     * @param consumerId The ID to set; might be null.
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    /**
     * Returns the metrics this consumer trail has received.
     *
     * @return A list of metrics, might be null
     */
    public List<DbMetric> getMetrics() {
        return metrics;
    }

    /**
     * Sets the metrics this consumer trail has received.
     *
     * @param metrics The list of metrics; might be null
     */
    public void setMetrics(List<DbMetric> metrics) {
        this.metrics = metrics;
    }
}
