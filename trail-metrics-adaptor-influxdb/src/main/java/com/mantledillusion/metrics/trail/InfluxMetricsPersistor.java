package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricFields;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Metric}s into an InfluxDB database
 * as {@link Point}s.
 */
public class InfluxMetricsPersistor implements MetricsConsumer {

    private static final BigDecimal NANOS_PER_SECOND = new BigDecimal(1000000000L);

    private final InfluxDB db;

    private InfluxMetricsPersistor(InfluxDB db) {
        this.db = db;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Metric metric) {
        Point.Builder pointBuilder = Point.measurement(metric.getIdentifier())
                .time(getNanos(metric.getTimestamp()), TimeUnit.NANOSECONDS)
                .tag(MetricFields.CONSUMER_ID.getName("_"), consumerId)
                .tag(MetricFields.CORRELATION_ID.getName("_"), correlationId.toString())
                .addField(MetricFields.TYPE.getName("_"), metric.getType().name());

        if (metric.getAttributes() != null) {
            for (MetricAttribute attribute: metric.getAttributes()) {
                if (Metric.OPERATOR_ATTRIBUTE_KEY.equals(attribute.getKey())) {
                    switch (metric.getType()) {
                        case METER:
                        case TREND:
                            pointBuilder.addField(Metric.OPERATOR_ATTRIBUTE_KEY, Float.parseFloat(attribute.getValue()));
                            break;
                        case PHASE:
                            pointBuilder.tag(attribute.getKey(), attribute.getValue());
                            break;
                    }
                } else {
                    pointBuilder.tag(attribute.getKey(), attribute.getValue());
                }
            }
        }

        this.db.write(pointBuilder.build());
    }

    private static Number getNanos(ZonedDateTime timeStamp) {
        Instant instant = timeStamp.toInstant();
        BigDecimal seconds = new BigDecimal(instant.getEpochSecond());
        BigDecimal nanos = seconds.multiply(NANOS_PER_SECOND);
        return nanos.add(new BigDecimal(instant.getNano()));
    }

    /**
     * Factory method for {@link InfluxMetricsPersistor}s.
     * <p>
     * Will use the given {@link InfluxDB} to persist consumed metrics.
     *
     * @param db The {@link InfluxDB} to persist into; might <b>not</b> be null.
     * @return A new {@link InfluxMetricsPersistor} instance, never null
     */
    public static InfluxMetricsPersistor from(InfluxDB db) {
        if (db == null) {
            throw new IllegalArgumentException("Cannot create a metrics persistor from a null database.");
        }
        return new InfluxMetricsPersistor(db);
    }
}
