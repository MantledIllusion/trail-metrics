package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class DbConverterTest {

    private static final String IDENTIFIER = "a.b.c";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_VALUE = "value";
    private static final LocalDate DATE = LocalDate.of(1994, 12, 3);
    private static final LocalTime TIME = LocalTime.of(6, 30, 00);
    private static final LocalDateTime DATETIME = LocalDateTime.of(DATE, TIME);
    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(DATE, TIME, ZONE);

    private static final ZoneIdStringAttributeConverter CONVERTER = new ZoneIdStringAttributeConverter();

    @Test
    public void testDbConversion() {
        // CREATE SOURCE OBJECT
        Metric source = new Metric(IDENTIFIER, MetricType.ALERT);
        source.setTimestamp(TIMESTAMP);

        MetricAttribute sourceAttribute = new MetricAttribute(ATTR_KEY, ATTR_VALUE);
        source.getAttributes().add(sourceAttribute);

        // MAP TO DB TARGET
        DbMetric target = DbMetric.from(source);

        // VALIDATE DB TARGET
        Assertions.assertEquals(IDENTIFIER, target.getIdentifier());
        Assertions.assertEquals(MetricType.ALERT, target.getType());
        Assertions.assertEquals(DATETIME, target.getTimestamp());
        Assertions.assertEquals(ZONE, target.getTimezone());

        String timeZone = CONVERTER.convertToDatabaseColumn(target.getTimezone());
        Assertions.assertEquals(ZONE, CONVERTER.convertToEntityAttribute(timeZone));

        Assertions.assertNotNull(target.getAttributes());
        Assertions.assertEquals(1, target.getAttributes().size());

        DbMetricAttribute targetAttribute = target.getAttributes().get(0);
        Assertions.assertSame(target, targetAttribute.getMetric());
        Assertions.assertEquals(ATTR_KEY, targetAttribute.getKey());
        Assertions.assertEquals(ATTR_VALUE, targetAttribute.getValue());

        // MAP BACK TO SOURCE
        Metric persisted = target.to();

        // VALIDATE RETRIEVED SOURCE
        Assertions.assertEquals(IDENTIFIER, persisted.getIdentifier());
        Assertions.assertEquals(MetricType.ALERT, persisted.getType());
        Assertions.assertEquals(TIMESTAMP, persisted.getTimestamp());

        Assertions.assertNotNull(persisted.getAttributes());
        Assertions.assertEquals(1, persisted.getAttributes().size());

        MetricAttribute persistedAttribute = persisted.getAttributes().get(0);
        Assertions.assertEquals(ATTR_KEY, persistedAttribute.getKey());
        Assertions.assertEquals(ATTR_VALUE, persistedAttribute.getValue());
    }
}
