package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.EventFields;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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

    private final RestHighLevelClient client;
    private IndexMode indexMode = IndexMode.IDENTIFIER;
    private String indexPrefix = DEFAULT_INDEX_PREFIX;

    private ElasticMetricsPersistor(RestHighLevelClient client) {
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
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field(EventFields.CONSUMER_ID.getName(), consumerId)
                .field(EventFields.CORRELATION_ID.getName(), correlationId.toString())
                .field(EventFields.IDENTIFIER.getName(), event.getIdentifier());

        if (event.getMeasurements() != null) {
            for (Measurement measurement : event.getMeasurements()) {
                String name = EventFields.MEASUREMENTS.getName() + '.' + measurement.getKey();
                switch (measurement.getType()) {
                    case BOOLEAN:
                        builder.field(name, (Boolean) measurement.parseValue());
                        break;
                    case SHORT:
                        builder.field(name, (Short) measurement.parseValue());
                        break;
                    case INTEGER:
                        builder.field(name, (Integer) measurement.parseValue());
                        break;
                    case LONG:
                        builder.field(name, (Long) measurement.parseValue());
                        break;
                    case FLOAT:
                        builder.field(name, (Float) measurement.parseValue());
                        break;
                    case DOUBLE:
                        builder.field(name, (Double) measurement.parseValue());
                        break;
                    case BIGINTEGER:
                        builder.field(name, (BigInteger) measurement.parseValue());
                        break;
                    case BIGDECIMAL:
                        builder.field(name, (BigDecimal) measurement.parseValue());
                        break;
                    case LOCAL_DATE:
                        builder.field(name, (LocalDate) measurement.parseValue());
                        break;
                    case LOCAL_TIME:
                        builder.field(name, (LocalTime) measurement.parseValue());
                        break;
                    case LOCAL_DATETIME:
                        builder.field(name, (LocalDateTime) measurement.parseValue());
                        break;
                    case ZONED_DATETIME:
                        builder.field(name, (ZonedDateTime) measurement.parseValue());
                        break;
                    default:
                        builder.field(name, measurement.getValue());
                        break;
                }
            }
        }

        String index = this.indexPrefix;
        switch (this.indexMode) {
            case IDENTIFIER:
                index += event.getIdentifier();
                break;
            case CONSUMER:
                index = consumerId;
                break;
        }

        this.client.index(new IndexRequest()
                .index(index)
                .source(builder
                        .timeField(EventFields.TIMESTAMP.getName(), event.getTimestamp())
                        .endObject()),
                RequestOptions.DEFAULT);
    }

    /**
     * Factory method for {@link ElasticMetricsPersistor}s.
     * <p>
     * Will use the given {@link RestHighLevelClient} to persist consumed metrics.
     *
     * @param client The {@link RestHighLevelClient} to persist into; might <b>not</b> be null.
     * @return A new {@link ElasticMetricsPersistor} instance, never null
     */
    public static ElasticMetricsPersistor from(RestHighLevelClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Cannot create a metrics persistor from a null client.");
        }
        return new ElasticMetricsPersistor(client);
    }
}
