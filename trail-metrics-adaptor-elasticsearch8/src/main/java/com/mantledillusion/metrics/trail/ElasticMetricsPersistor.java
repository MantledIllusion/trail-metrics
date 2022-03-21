package com.mantledillusion.metrics.trail;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.EventFields;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Event}s into an Elastic database.
 */
public class ElasticMetricsPersistor implements MetricsConsumer {

    public static final String DEFAULT_INDEX_PREFIX = "metrics.";

    /**
     * Describes the modes the elastic index to push to is determined by.
     */
    public enum IndexMode {

        /**
         * The index will be the {@link Event}'s identifier, prepended by the prefix currently set.
         */
        IDENTIFIER,

        /**
         * The index will be the {@link Event}'s consumer's ID, prepended by the prefix currently set.
         */
        CONSUMER,

        /**
         * The index will be just the prefix by itself.
         */
        STATIC
    }

    private final ElasticsearchClient client;
    private IndexMode indexMode = IndexMode.IDENTIFIER;
    private String indexPrefix = DEFAULT_INDEX_PREFIX;

    private ElasticMetricsPersistor(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * Returns the mode determining the index the metrics are pushed to.
     * <p>
     * Set to {@link IndexMode#IDENTIFIER} by default.
     *
     * @return The index mode, never null
     */
    public IndexMode getIndexMode() {
        return this.indexMode;
    }

    /**
     * Returns the prefix prepended to metric identifiers when used for an elastic index.
     * <p>
     * Set to {@value #DEFAULT_INDEX_PREFIX} by default.
     *
     * @return The prefix, never null
     */
    public String getIndexPrefix() {
        return this.indexPrefix;
    }

    /**
     * Sets the index mode and prefix to use when pushing metrics to elastic.
     *
     * @param mode The mode to use when determining the index; might <b>not</b> be null.
     * @param prefix The prefix to prepend to the index determined; might be null unless the mode is {@link IndexMode#STATIC}.
     */
    public void setIndexMode(IndexMode mode, String prefix) {
        prefix = prefix == null ? "" : prefix;
        if (mode == null) {
            throw new IllegalArgumentException("Cannot set a null index mode");
        } else if (mode == IndexMode.STATIC && prefix.isEmpty()) {
            throw  new IllegalArgumentException("Cannot use an empty prefix with the STATIC index mode");
        }
        this.indexMode = mode;
        this.indexPrefix = prefix;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Event event) throws IOException {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add(EventFields.CONSUMER_ID.getName(), consumerId)
                .add(EventFields.CORRELATION_ID.getName(), correlationId.toString())
                .add(EventFields.IDENTIFIER.getName(), event.getIdentifier());

        if (event.getMeasurements() != null) {
            for (Measurement measurement : event.getMeasurements()) {
                String name = EventFields.MEASUREMENTS.getName() + '.' + measurement.getKey();
                switch (measurement.getType()) {
                    case BOOLEAN:
                        builder.add(name, (Boolean) measurement.parseValue());
                        break;
                    case SHORT:
                        builder.add(name, (Short) measurement.parseValue());
                        break;
                    case INTEGER:
                        builder.add(name, (Integer) measurement.parseValue());
                        break;
                    case LONG:
                        builder.add(name, (Long) measurement.parseValue());
                        break;
                    case FLOAT:
                        builder.add(name, (Float) measurement.parseValue());
                        break;
                    case DOUBLE:
                        builder.add(name, (Double) measurement.parseValue());
                        break;
                    case BIGINTEGER:
                        builder.add(name, (BigInteger) measurement.parseValue());
                        break;
                    case BIGDECIMAL:
                        builder.add(name, (BigDecimal) measurement.parseValue());
                        break;
                    case LOCAL_DATE:
                        LocalDate localDate = LocalDate.parse(measurement.getValue());
                        builder.add(name, DateTimeFormatter.ISO_LOCAL_DATE.format(localDate));
                        break;
                    case LOCAL_TIME:
                        LocalTime localTime = LocalTime.parse(measurement.getValue());
                        builder.add(name, DateTimeFormatter.ISO_LOCAL_TIME.format(localTime));
                        break;
                    case LOCAL_DATETIME:
                        LocalDateTime localDateTime = LocalDateTime.parse(measurement.getValue());
                        builder.add(name, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
                        break;
                    case ZONED_DATETIME:
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(measurement.getValue());
                        builder.add(name, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
                        break;
                    default:
                        builder.add(name, measurement.getValue());
                        break;
                }
            }
        }

        String index;
        switch (this.indexMode) {
            case IDENTIFIER:
                index = this.indexPrefix + event.getIdentifier();
                break;
            case CONSUMER:
                index = consumerId;
                break;
            default:
                index = this.indexPrefix;
        }

        this.client.index(IndexRequest.of(b -> b
                .index(index)
                .document(builder
                        .add(EventFields.TIMESTAMP.getName(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(event.getTimestamp()))
                        .build())));
    }

    /**
     * Factory method for {@link ElasticMetricsPersistor}s.
     * <p>
     * Will use the given {@link ElasticsearchClient} to persist consumed metrics.
     *
     * @param client The {@link ElasticsearchClient} to persist into; might <b>not</b> be null.
     * @return A new {@link ElasticMetricsPersistor} instance, never null
     */
    public static ElasticMetricsPersistor from(ElasticsearchClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Cannot create a metrics persistor from a null client.");
        }
        return new ElasticMetricsPersistor(client);
    }
}
