package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricAttribute;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;
import org.h2.tools.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

    private static EntityManager ENTITY_MANAGER;
    private static MetricsPersistor PERSISTOR;

    @BeforeAll
    public static void beforeAll() throws SQLException, URISyntaxException, IOException {
        Server.createTcpServer().start();

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
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
        Metric metric = new Metric();
        metric.setType(MetricType.ALERT);
        metric.setTimestamp(ZonedDateTime.now());
        metric.setIdentifier(METRIC_IDENTIFIER_PREFIX+1);

        PERSISTOR.consume(CONSUMER_ID, correlationId, metric);
        assertMatching(correlationId, metric, 1, 1, 0, 1, 0);

        // METRIC #2 (TRAIL #1)
        metric.setIdentifier(METRIC_IDENTIFIER_PREFIX+2);
        metric.getAttributes().add(new MetricAttribute(METRIC_ATTRIBUTE_KEY, METRIC_ATTRIBUTE_VALUE));

        PERSISTOR.consume(CONSUMER_ID, correlationId, metric);
        assertMatching(correlationId, metric, 2, 1, 0, 2, 1);

        // TRAIL #2
        correlationId = UUID.randomUUID();

        // METRIC #3 (TRAIL #2)
        metric.setIdentifier(METRIC_IDENTIFIER_PREFIX+3);
        metric.getAttributes().clear();

        PERSISTOR.consume(CONSUMER_ID, correlationId, metric);
        assertMatching(correlationId, metric, 3, 2, 1, 1, 0);
    }

    private void assertMatching(UUID correlationId, Metric metric, int metricNumber,
                                int expectedTrailCount, int matchingCorrelationIdx,
                                int expectedMetricCount, int matchingMetricIdx) {
        List<DbMetricsConsumerTrail> dbTrails = ENTITY_MANAGER.
                createQuery("SELECT t FROM DbMetricsConsumerTrail t", DbMetricsConsumerTrail.class).getResultList();

        Assertions.assertNotNull(dbTrails);
        Assertions.assertEquals(expectedTrailCount, dbTrails.size());

        DbMetricsConsumerTrail dbTrail = dbTrails.get(matchingCorrelationIdx);
        Assertions.assertEquals(CONSUMER_ID, dbTrail.getConsumerId());
        Assertions.assertEquals(correlationId, dbTrail.getCorrelationId());
        Assertions.assertNotNull(dbTrail.getMetrics());
        Assertions.assertEquals(expectedMetricCount, dbTrail.getMetrics().size());

        DbMetric dbMetric = dbTrail.getMetrics().get(matchingMetricIdx);
        Assertions.assertNotNull(dbMetric.getTrail());
        Assertions.assertEquals(dbTrail.getId(), dbMetric.getTrail().getId());
        Assertions.assertEquals(METRIC_IDENTIFIER_PREFIX+metricNumber, dbMetric.getIdentifier());
        Assertions.assertEquals(MetricType.ALERT, dbMetric.getType());
        Assertions.assertTrue(metric.getAttributes().size() == 0 ? dbMetric.getAttributes() == null || dbMetric.getAttributes().isEmpty() :
                dbMetric.getAttributes().size() == metric.getAttributes().size());

        Map<String, String> attributeMap = metric.getAttributes().stream().
                collect(Collectors.toMap(MetricAttribute::getKey, MetricAttribute::getValue));
        if (dbMetric.getAttributes() != null) {
            for (DbMetricAttribute attribute: dbMetric.getAttributes()) {
                Assertions.assertTrue(attributeMap.containsKey(attribute.getKey()));
                Assertions.assertEquals(attributeMap.get(attribute.getKey()), attribute.getValue());

                attributeMap.remove(attribute.getKey());
            }
        }
        Assertions.assertTrue(attributeMap.isEmpty());
    }
}
