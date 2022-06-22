package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MeasurementType;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailEvent;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailMeasurement;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailConsumer;
import com.mantledillusion.metrics.trail.repositories.MeasurementRepository;
import com.mantledillusion.metrics.trail.repositories.EventRepository;
import com.mantledillusion.metrics.trail.repositories.ConsumerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@SpringBootTest
public class TrailMetricsHibernateJpaAutoConfigurationTest {

    private static final UUID TRAIL_ID = UUID.randomUUID();
    private static final String TRAIL_CONSUMER_ID = "consumerId";
    private static final String METRIC_IDENTIFIER = "metricId";
    private static final LocalDateTime METRIC_TIMESTAMP = LocalDateTime.now();
    private static final ZoneId METRIC_TIMEZONE = ZoneId.systemDefault();
    private static final String ATTRIBUTE_KEY = "attributeKey";
    private static final String ATTRIBUTE_VALUE = "attributeValue";

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ConsumerRepository metricsConsumerRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private MeasurementRepository measurementRepository;

    @Test
    public void testPersist() {
        this.transactionTemplate.executeWithoutResult(transactionStatus -> {
            DbTrailConsumer consumer = new DbTrailConsumer();
            consumer.setCorrelationId(TRAIL_ID);
            consumer.setConsumerId(TRAIL_CONSUMER_ID);

            DbTrailEvent event = new DbTrailEvent();
            event.setTrail(consumer);
            event.setIdentifier(METRIC_IDENTIFIER);
            event.setTimestamp(METRIC_TIMESTAMP);
            event.setTimezone(METRIC_TIMEZONE);
            consumer.setEvents(Collections.singletonList(event));

            DbTrailMeasurement measurement = new DbTrailMeasurement();
            measurement.setEvent(event);
            measurement.setKey(ATTRIBUTE_KEY);
            measurement.setValue(ATTRIBUTE_VALUE);
            measurement.setType(MeasurementType.STRING);
            event.setMeasurements(Collections.singletonList(measurement));

            this.metricsConsumerRepository.saveAndFlush(consumer);
        });

        this.transactionTemplate.executeWithoutResult(transactionStatus -> {
            Assertions.assertEquals(1L, this.metricsConsumerRepository.count());
            DbTrailConsumer consumer = this.metricsConsumerRepository.findAll().iterator().next();
            Assertions.assertNotNull(consumer.getId());
            Assertions.assertEquals(TRAIL_CONSUMER_ID, consumer.getConsumerId());
            Assertions.assertEquals(TRAIL_ID, consumer.getCorrelationId());

            Assertions.assertEquals(1, consumer.getEvents().size());
            DbTrailEvent event = consumer.getEvents().iterator().next();
            Assertions.assertNotNull(event.getId());
            Assertions.assertSame(consumer, event.getTrail());
            Assertions.assertEquals(METRIC_IDENTIFIER, event.getIdentifier());
            Assertions.assertEquals(METRIC_TIMESTAMP, event.getTimestamp());
            Assertions.assertEquals(METRIC_TIMEZONE, event.getTimezone());

            Assertions.assertEquals(1, event.getMeasurements().size());
            DbTrailMeasurement measurement = event.getMeasurements().iterator().next();
            Assertions.assertNotNull(measurement.getId());
            Assertions.assertSame(event, measurement.getEvent());
            Assertions.assertEquals(ATTRIBUTE_KEY, measurement.getKey());
            Assertions.assertEquals(ATTRIBUTE_VALUE, measurement.getValue());
        });
    }

    @Test
    public void testClean() {
        DbTrailConsumer consumer = new DbTrailConsumer();
        consumer.setCorrelationId(UUID.randomUUID());
        consumer.setConsumerId(TRAIL_CONSUMER_ID);

        DbTrailEvent event = new DbTrailEvent();
        event.setTrail(consumer);
        event.setIdentifier("cleanme.abc");
        event.setTimestamp(LocalDateTime.now());
        event.setTimezone(METRIC_TIMEZONE);
        consumer.setEvents(Collections.singletonList(event));

        this.metricsConsumerRepository.saveAndFlush(consumer);

        long ms = System.currentTimeMillis();
        this.transactionTemplate.executeWithoutResult(transactionStatus -> {
            while (this.eventRepository.countByIdentifier("cleanme.%") != 0) {
                if (System.currentTimeMillis()-ms < 5000) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Assertions.fail();
                }
            }
        });
    }
}
