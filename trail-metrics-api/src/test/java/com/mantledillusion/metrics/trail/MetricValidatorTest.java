package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricValidatorTest {

    private static final String IDENTIFIER = "a.b.c";

    @Test
    public void testValidateNullMetric() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(null));
    }

    @Test
    public void testValidateMetricWithoutIdentifier() {
        Metric metric = new Metric(null, MetricType.ALERT);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateMetricWithoutType() {
        Metric metric = new Metric(IDENTIFIER, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateMetricWithoutTimestamp() {
        Metric metric = new Metric(IDENTIFIER, MetricType.ALERT);
        metric.setTimestamp(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateMetricWithNullAttribute() {
        Metric metric = new Metric(IDENTIFIER, MetricType.ALERT);
        metric.getAttributes().add(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateMeterMetricWithoutOperator() {
        Metric metric = new Metric(IDENTIFIER, MetricType.METER);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateMeterMetricWithNonFloatOperator() {
        Metric metric = new Metric(IDENTIFIER, MetricType.METER, "someStringOperator");
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateTrendMetricWithoutOperator() {
        Metric metric = new Metric(IDENTIFIER, MetricType.TREND);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }

    @Test
    public void testValidateTrendMetricWithNonFloatOperator() {
        Metric metric = new Metric(IDENTIFIER, MetricType.TREND, "someStringOperator");
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricValidator.validate(metric));
    }
}
