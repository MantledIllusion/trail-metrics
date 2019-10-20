package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.StaticLoggerBinder;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import java.util.function.Function;

public class MetricsLoggerTest {

    private static final String CONSUMER_ID = "consumerName";
    private static final UUID TRAIL_ID = UUID.randomUUID();

    private static final String ID_A = "identifierA";
    private static final String ID_B = "identifierB";
    private static final String ID_C = "identifierC";

    private static final Metric METRIC_A = new Metric(ID_A, MetricType.ALERT);
    private static final Metric METRIC_B = new Metric(ID_B, MetricType.ALERT);
    private static final Metric METRIC_C = new Metric(ID_C, MetricType.METER, MetricAttribute.operatorOf(1337));

    @BeforeEach
    public void before() {
        StaticLoggerBinder.hook();
    }

    @Test
    public void testDefaultLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_C);

        Assertions.assertEquals(1, StaticLoggerBinder.count());
        Assertions.assertSame(Level.INFO, StaticLoggerBinder.getLevel(0));

        String msg = StaticLoggerBinder.getLog(0);
        Assertions.assertTrue(msg.contains(CONSUMER_ID));
        Assertions.assertTrue(msg.contains(TRAIL_ID.toString()));
        Assertions.assertTrue(msg.contains(METRIC_C.getIdentifier()));
        Assertions.assertTrue(msg.contains(METRIC_C.getType().name()));
        Assertions.assertTrue(msg.contains(METRIC_C.getTimestamp().toString()));
    }

    @Test
    public void testSpecificDefaultLevelLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().setDefaultLevel(Level.WARN).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(0));
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(1));
    }

    @Test
    public void testSpecificIdentifierLevelLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().setMetricLevel(ID_B, Level.WARN).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertSame(Level.INFO, StaticLoggerBinder.getLevel(0));
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(1));
    }

    @Test
    public void testSpecificDateTimeRendererLogging() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");

        MetricsLogger logger = MetricsLogger.from().setDateTimeRenderer(formatter::format).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).contains(formatter.format(METRIC_A.getTimestamp())));
        Assertions.assertTrue(StaticLoggerBinder.getLog(1).contains(formatter.format(METRIC_B.getTimestamp())));
    }

    @Test
    public void testSpecificDefaultMessageLogging() throws Exception {
        Function<Metric, String> renderer = metric -> metric.getIdentifier()+"X";

        MetricsLogger logger = MetricsLogger.from().setDefaultMessageRenderer(renderer).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).endsWith(renderer.apply(METRIC_A)));
        Assertions.assertTrue(StaticLoggerBinder.getLog(1).endsWith(renderer.apply(METRIC_B)));
    }

    @Test
    public void testSpecificIdentifierMessageLogging() throws Exception {
        Function<Metric, String> renderer = metric -> metric.getIdentifier()+"X";

        MetricsLogger logger = MetricsLogger.from().setMetricRenderer(METRIC_A.getIdentifier(), renderer).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, METRIC_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).endsWith(renderer.apply(METRIC_A)));
        Assertions.assertFalse(StaticLoggerBinder.getLog(1).endsWith(renderer.apply(METRIC_B)));
    }
}
