package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MeasurementType;
import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailEvent;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailMeasurement;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailConsumer;
import org.h2.tools.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsPersistorTest {

    private static final String CONSUMER_ID = "test";
    private static final String METRIC_IDENTIFIER_PREFIX = "metric#";
    private static final String METRIC_ATTRIBUTE_KEY = "attrKey";
    private static final String METRIC_ATTRIBUTE_VALUE = "attrValue";
    private static final MeasurementType METRIC_ATTRIBUTE_TYPE = MeasurementType.STRING;

    private static EntityManager ENTITY_MANAGER;
    private static MetricsPersistor PERSISTOR;

    @BeforeAll
    public static void beforeAll() throws SQLException, URISyntaxException, IOException {
        Server.createTcpServer().start();

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaPersistenceUnit", properties);
        ENTITY_MANAGER = entityManagerFactory.createEntityManager();

        PERSISTOR = MetricsPersistor.from(ENTITY_MANAGER);

        String schemaSql = new String(Files.readAllBytes(Paths.get(MetricsPersistor.class.getResource("/schema/init_metrics_schema.sql").toURI())));

        ENTITY_MANAGER.getTransaction().begin();
        ENTITY_MANAGER.createNativeQuery(schemaSql).executeUpdate();
        ENTITY_MANAGER.getTransaction().commit();
    }

    @Test
    public void consumeMetricIntoDb() {
        // TRAIL #1
        UUID correlationId = UUID.randomUUID();

        // METRIC #1 (TRAIL #1)
        Event event = new Event();
        event.setTimestamp(ZonedDateTime.now());
        event.setIdentifier(METRIC_IDENTIFIER_PREFIX+1);

        PERSISTOR.consume(CONSUMER_ID, correlationId, event);
        assertMatching(correlationId, event, 1, 1, 0, 1, 0);

        // METRIC #2 (TRAIL #1)
        event.setIdentifier(METRIC_IDENTIFIER_PREFIX+2);
        event.getMeasurements().add(new Measurement(METRIC_ATTRIBUTE_KEY, METRIC_ATTRIBUTE_VALUE, METRIC_ATTRIBUTE_TYPE));

        PERSISTOR.consume(CONSUMER_ID, correlationId, event);
        assertMatching(correlationId, event, 2, 1, 0, 2, 1);

        // TRAIL #2
        correlationId = UUID.randomUUID();

        // METRIC #3 (TRAIL #2)
        event.setIdentifier(METRIC_IDENTIFIER_PREFIX+3);
        event.getMeasurements().clear();

        PERSISTOR.consume(CONSUMER_ID, correlationId, event);
        assertMatching(correlationId, event, 3, 2, 1, 1, 0);
    }

    private void assertMatching(UUID correlationId, Event event, int metricNumber,
                                int expectedTrailCount, int matchingCorrelationIdx,
                                int expectedMetricCount, int matchingMetricIdx) {
        List<DbTrailConsumer> dbTrails = ENTITY_MANAGER.
                createQuery("SELECT t FROM DbTrailConsumer t", DbTrailConsumer.class).getResultList();

        Assertions.assertNotNull(dbTrails);
        Assertions.assertEquals(expectedTrailCount, dbTrails.size());

        DbTrailConsumer dbTrail = dbTrails.get(matchingCorrelationIdx);
        Assertions.assertEquals(CONSUMER_ID, dbTrail.getConsumerId());
        Assertions.assertEquals(correlationId, dbTrail.getCorrelationId());
        Assertions.assertNotNull(dbTrail.getEvents());
        Assertions.assertEquals(expectedMetricCount, dbTrail.getEvents().size());

        DbTrailEvent dbTrailEvent = dbTrail.getEvents().get(matchingMetricIdx);
        Assertions.assertNotNull(dbTrailEvent.getTrail());
        Assertions.assertEquals(dbTrail.getId(), dbTrailEvent.getTrail().getId());
        Assertions.assertEquals(METRIC_IDENTIFIER_PREFIX+metricNumber, dbTrailEvent.getIdentifier());
        Assertions.assertTrue(event.getMeasurements().size() == 0 ? dbTrailEvent.getMeasurements() == null || dbTrailEvent.getMeasurements().isEmpty() :
                dbTrailEvent.getMeasurements().size() == event.getMeasurements().size());

        Map<String, String> attributeMap = event.getMeasurements().stream().
                collect(Collectors.toMap(Measurement::getKey, Measurement::getValue));
        if (dbTrailEvent.getMeasurements() != null) {
            for (DbTrailMeasurement attribute: dbTrailEvent.getMeasurements()) {
                Assertions.assertTrue(attributeMap.containsKey(attribute.getKey()));
                Assertions.assertEquals(attributeMap.get(attribute.getKey()), attribute.getValue());
                Assertions.assertEquals(MeasurementType.STRING, attribute.getType());

                attributeMap.remove(attribute.getKey());
            }
        }
        Assertions.assertTrue(attributeMap.isEmpty());
    }
}
