package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricAttribute;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;
import com.mantledillusion.metrics.trail.repositories.MetricAttributeRepository;
import com.mantledillusion.metrics.trail.repositories.MetricRepository;
import com.mantledillusion.metrics.trail.repositories.MetricsConsumerTrailRepository;
import org.junit.Assert;
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
    private MetricsConsumerTrailRepository metricsConsumerRepository;
    @Autowired
    private MetricRepository metricRepository;
    @Autowired
    private MetricAttributeRepository metricAttributeRepository;

    @Test
    public void test() {
        this.transactionTemplate.executeWithoutResult(transactionStatus -> {
            DbMetricsConsumerTrail trail = new DbMetricsConsumerTrail();
            trail.setTrailId(TRAIL_ID);
            trail.setConsumerId(TRAIL_CONSUMER_ID);

            DbMetric metric = new DbMetric();
            metric.setTrail(trail);
            metric.setIdentifier(METRIC_IDENTIFIER);
            metric.setType(MetricType.ALERT);
            metric.setTimestamp(METRIC_TIMESTAMP);
            metric.setTimezone(METRIC_TIMEZONE);
            trail.setMetrics(Collections.singletonList(metric));

            DbMetricAttribute attribute = new DbMetricAttribute();
            attribute.setMetric(metric);
            attribute.setKey(ATTRIBUTE_KEY);
            attribute.setValue(ATTRIBUTE_VALUE);
            metric.setAttributes(Collections.singletonList(attribute));

            this.metricsConsumerRepository.saveAndFlush(trail);
        });


        this.transactionTemplate.executeWithoutResult(transactionStatus -> {
            Assertions.assertEquals(1L, this.metricsConsumerRepository.count());
            DbMetricsConsumerTrail trail = this.metricsConsumerRepository.findAll().iterator().next();
            Assertions.assertNotNull(trail.getId());
            Assertions.assertEquals(TRAIL_CONSUMER_ID, trail.getConsumerId());
            Assertions.assertEquals(TRAIL_ID, trail.getTrailId());

            Assertions.assertEquals(1, trail.getMetrics().size());
            DbMetric metric = trail.getMetrics().iterator().next();
            Assertions.assertNotNull(metric.getId());
            Assert.assertSame(trail, metric.getTrail());
            Assertions.assertEquals(METRIC_IDENTIFIER, metric.getIdentifier());
            Assertions.assertEquals(MetricType.ALERT, metric.getType());
            Assertions.assertEquals(METRIC_TIMESTAMP, metric.getTimestamp());
            Assertions.assertEquals(METRIC_TIMEZONE, metric.getTimezone());

            Assertions.assertEquals(1, metric.getAttributes().size());
            DbMetricAttribute attribute = metric.getAttributes().iterator().next();
            Assertions.assertNotNull(attribute.getId());
            Assert.assertSame(metric, attribute.getMetric());
            Assertions.assertEquals(ATTRIBUTE_KEY, attribute.getKey());
            Assertions.assertEquals(ATTRIBUTE_VALUE, attribute.getValue());
        });
    }
}
