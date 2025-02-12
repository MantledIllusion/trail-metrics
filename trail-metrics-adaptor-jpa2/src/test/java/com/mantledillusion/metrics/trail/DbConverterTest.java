package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.MeasurementType;
import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailEvent;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailMeasurement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class DbConverterTest {

    private static final String IDENTIFIER = "a.b.c";
    private static final String ATTR_KEY = "key";
    private static final String ATTR_VALUE = "value";
    private static final MeasurementType ATTR_TYPE = MeasurementType.STRING;
    private static final LocalDate DATE = LocalDate.of(1994, 12, 3);
    private static final LocalTime TIME = LocalTime.of(6, 30, 00);
    private static final LocalDateTime DATETIME = LocalDateTime.of(DATE, TIME);
    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(DATE, TIME, ZONE);

    private static final ZoneIdStringAttributeConverter CONVERTER = new ZoneIdStringAttributeConverter();

    @Test
    public void testDbConversion() {
        // CREATE SOURCE OBJECT
        Event source = new Event(IDENTIFIER);
        source.setTimestamp(TIMESTAMP);

        Measurement sourceMeasurement = new Measurement(ATTR_KEY, ATTR_VALUE, ATTR_TYPE);
        source.getMeasurements().add(sourceMeasurement);

        // MAP TO DB TARGET
        DbTrailEvent target = DbTrailEvent.from(source);

        // VALIDATE DB TARGET
        Assertions.assertEquals(IDENTIFIER, target.getIdentifier());
        Assertions.assertEquals(DATETIME, target.getTimestamp());
        Assertions.assertEquals(ZONE, target.getTimezone());

        String timeZone = CONVERTER.convertToDatabaseColumn(target.getTimezone());
        Assertions.assertEquals(ZONE, CONVERTER.convertToEntityAttribute(timeZone));

        Assertions.assertNotNull(target.getMeasurements());
        Assertions.assertEquals(1, target.getMeasurements().size());

        DbTrailMeasurement targetAttribute = target.getMeasurements().get(0);
        Assertions.assertSame(target, targetAttribute.getEvent());
        Assertions.assertEquals(ATTR_KEY, targetAttribute.getKey());
        Assertions.assertEquals(ATTR_VALUE, targetAttribute.getValue());
        Assertions.assertEquals(ATTR_TYPE, targetAttribute.getType());

        // MAP BACK TO SOURCE
        Event persisted = target.to();

        // VALIDATE RETRIEVED SOURCE
        Assertions.assertEquals(IDENTIFIER, persisted.getIdentifier());
        Assertions.assertEquals(TIMESTAMP, persisted.getTimestamp());

        Assertions.assertNotNull(persisted.getMeasurements());
        Assertions.assertEquals(1, persisted.getMeasurements().size());

        Measurement persistedMeasurement = persisted.getMeasurements().get(0);
        Assertions.assertEquals(ATTR_KEY, persistedMeasurement.getKey());
        Assertions.assertEquals(ATTR_VALUE, persistedMeasurement.getValue());
        Assertions.assertEquals(ATTR_TYPE, persistedMeasurement.getType());
    }
}
