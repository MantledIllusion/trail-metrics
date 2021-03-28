package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventValidatorTest {

    private static final String IDENTIFIER = "a.b.c";

    @Test
    public void testValidateNullMetric() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventValidator.validate(null));
    }

    @Test
    public void testValidateMetricWithoutIdentifier() {
        Event event = new Event(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventValidator.validate(event));
    }

    @Test
    public void testValidateMetricWithoutTimestamp() {
        Event event = new Event(IDENTIFIER);
        event.setTimestamp(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventValidator.validate(event));
    }

    @Test
    public void testValidateMetricWithNullAttribute() {
        Event event = new Event(IDENTIFIER);
        event.getMeasurements().add(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventValidator.validate(event));
    }
}
