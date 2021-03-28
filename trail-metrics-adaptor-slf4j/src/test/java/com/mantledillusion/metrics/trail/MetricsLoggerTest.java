package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MeasurementType;
import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.impl.StaticLoggerBinder;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;

public class MetricsLoggerTest {

    private static final String CONSUMER_ID = "consumerName";
    private static final UUID TRAIL_ID = UUID.randomUUID();

    private static final String ID_A = "identifierA";
    private static final String ID_B = "identifierB";
    private static final String ID_C = "identifierC";
    private static final String KEY_C_1 = "keyC1";

    private static final Event EVENT_A = new Event(ID_A);
    private static final Event EVENT_B = new Event(ID_B);
    private static final Event EVENT_C = new Event(ID_C, new Measurement(KEY_C_1, "1337", MeasurementType.INTEGER));

    @BeforeEach
    public void before() {
        StaticLoggerBinder.hook();
    }

    @Test
    public void testDefaultLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_C);

        Assertions.assertEquals(1, StaticLoggerBinder.count());
        Assertions.assertSame(Level.INFO, StaticLoggerBinder.getLevel(0));

        String msg = StaticLoggerBinder.getLog(0);
        Assertions.assertTrue(msg.contains(CONSUMER_ID));
        Assertions.assertTrue(msg.contains(TRAIL_ID.toString()));
        Assertions.assertTrue(msg.contains(EVENT_C.getIdentifier()));
        Assertions.assertTrue(msg.contains(EVENT_C.getTimestamp().toString()));
    }

    @Test
    public void testSpecificDefaultLevelLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().setDefaultLevel(Level.WARN).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(0));
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(1));
    }

    @Test
    public void testSpecificIdentifierLevelLogging() throws Exception {
        MetricsLogger logger = MetricsLogger.from().setMetricLevel(ID_B, Level.WARN).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertSame(Level.INFO, StaticLoggerBinder.getLevel(0));
        Assertions.assertSame(Level.WARN, StaticLoggerBinder.getLevel(1));
    }

    @Test
    public void testSpecificDateTimeRendererLogging() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");

        MetricsLogger logger = MetricsLogger.from().setDateTimeRenderer(formatter::format).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).contains(formatter.format(EVENT_A.getTimestamp())));
        Assertions.assertTrue(StaticLoggerBinder.getLog(1).contains(formatter.format(EVENT_B.getTimestamp())));
    }

    @Test
    public void testSpecificDefaultMessageLogging() throws Exception {
        Function<Event, String> renderer = metric -> metric.getIdentifier()+"X";

        MetricsLogger logger = MetricsLogger.from().setDefaultMessageRenderer(renderer).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).endsWith(renderer.apply(EVENT_A)));
        Assertions.assertTrue(StaticLoggerBinder.getLog(1).endsWith(renderer.apply(EVENT_B)));
    }

    @Test
    public void testSpecificIdentifierMessageLogging() throws Exception {
        Function<Event, String> renderer = metric -> metric.getIdentifier()+"X";

        MetricsLogger logger = MetricsLogger.from().setMetricRenderer(EVENT_A.getIdentifier(), renderer).build();
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_A);
        logger.consume(CONSUMER_ID, TRAIL_ID, EVENT_B);

        Assertions.assertEquals(2, StaticLoggerBinder.count());
        Assertions.assertTrue(StaticLoggerBinder.getLog(0).endsWith(renderer.apply(EVENT_A)));
        Assertions.assertFalse(StaticLoggerBinder.getLog(1).endsWith(renderer.apply(EVENT_B)));
    }
}
