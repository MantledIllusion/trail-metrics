package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricsTrailTest extends AbstractMetricsTest {

    @Test
    public void createTrailTest() {
        MetricsTrail trail = new MetricsTrail(TRAIL_ID);
        Assertions.assertEquals(TRAIL_ID, trail.getCorrelationId());
    }

    @Test
    public void createTrailTestWithoutId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetricsTrail(null));
    }

    @Test
    public void hookNullConsumer() {
        MetricsTrail trail = new MetricsTrail(TRAIL_ID);
        Assertions.assertThrows(IllegalArgumentException.class, () -> trail.hook(null));
    }

    @Test
    public void commitNullMetric() {
        MetricsTrail trail = new MetricsTrail(TRAIL_ID);
        Assertions.assertThrows(IllegalArgumentException.class, () -> trail.commit(null));
    }
}
