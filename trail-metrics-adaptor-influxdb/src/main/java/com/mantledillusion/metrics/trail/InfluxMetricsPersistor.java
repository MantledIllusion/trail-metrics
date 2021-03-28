package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.EventFields;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Event}s into an InfluxDB database
 * as {@link Point}s.
 */
public class InfluxMetricsPersistor implements MetricsConsumer {

    private static final BigDecimal NANOS_PER_SECOND = new BigDecimal(1000000000L);

    private final InfluxDB db;

    private InfluxMetricsPersistor(InfluxDB db) {
        this.db = db;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Event event) {
        Point.Builder pointBuilder = Point.measurement(event.getIdentifier())
                .time(getNanos(event.getTimestamp()), TimeUnit.NANOSECONDS)
                .tag(EventFields.CONSUMER_ID.getName("_"), consumerId)
                .tag(EventFields.CORRELATION_ID.getName("_"), correlationId.toString());

        if (event.getMeasurements() != null) {
            for (Measurement measurement : event.getMeasurements()) {
                switch (measurement.getType()) {
                    case BOOLEAN:
                        pointBuilder.addField(measurement.getKey(), (Boolean) measurement.parseValue());
                        break;
                    case SHORT:
                        pointBuilder.addField(measurement.getKey(), (Short) measurement.parseValue());
                        break;
                    case INTEGER:
                        pointBuilder.addField(measurement.getKey(), (Integer) measurement.parseValue());
                        break;
                    case LONG:
                        pointBuilder.addField(measurement.getKey(), (Long) measurement.parseValue());
                        break;
                    case FLOAT:
                        pointBuilder.addField(measurement.getKey(), (Float) measurement.parseValue());
                        break;
                    case DOUBLE:
                        pointBuilder.addField(measurement.getKey(), (Double) measurement.parseValue());
                        break;
                    case BIGINTEGER:
                        pointBuilder.addField(measurement.getKey(), ((BigInteger) measurement.parseValue()).longValue());
                        break;
                    case BIGDECIMAL:
                        pointBuilder.addField(measurement.getKey(), ((BigDecimal) measurement.parseValue()).doubleValue());
                        break;
                    default:
                        pointBuilder.addField(measurement.getKey(), measurement.getValue());
                        break;
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
