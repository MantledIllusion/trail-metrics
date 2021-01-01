package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Metric}s into an Elastic database.
 */
public class ElasticMetricsPersistor implements MetricsConsumer {

    private final RestHighLevelClient client;

    private ElasticMetricsPersistor(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Metric metric) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field(Metric.CONSUMER_ID_FIELD_KEY, consumerId)
                .field(Metric.CORRELATION_ID_FIELD_KEY, correlationId.toString())
                .field("type", metric.getType().name())
                .startArray(Metric.ATTRIBUTES_KEY);

        if (metric.getAttributes() != null) {
            for (MetricAttribute attribute : metric.getAttributes()) {
                builder = builder
                        .startObject()
                        .field(Metric.ATTRIBUTE_KEY, attribute.getKey())
                        .field(Metric.ATTRIBUTE_VALUE_KEY, attribute.getValue())
                        .endObject();
            }
        }

        this.client.index(new IndexRequest()
                .index(metric.getIdentifier())
                .source(builder
                        .endArray()
                        .timeField(Metric.TIMESTAMP_KEY, metric.getTimestamp())
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
