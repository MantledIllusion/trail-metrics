package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricsTrailTest extends AbstractMetricsTest {

    @Test
    public void beginRandomTrailTest() {
        Assertions.assertFalse(MetricsTrail.has());

        MetricsTrail.begin();

        Assertions.assertTrue(MetricsTrail.has());
        Assertions.assertNotNull(MetricsTrail.get());

        MetricsTrail.end();

        Assertions.assertFalse(MetricsTrail.has());
    }

    @Test
    public void beginSpecificTrailTest() {
        Assertions.assertFalse(MetricsTrail.has());

        MetricsTrail.begin(TRAIL_ID);

        Assertions.assertTrue(MetricsTrail.has());
        Assertions.assertEquals(TRAIL_ID, MetricsTrail.get());

        MetricsTrail.end();

        Assertions.assertFalse(MetricsTrail.has());
    }

    @Test
    public void beginTrailDuringExisting() {
        MetricsTrail.begin();
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.begin());
    }

    @Test
    public void beginTrailWithoutId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrail.begin(null));
    }

    @Test
    public void getTrailWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.get());
    }

    @Test
    public void hookConsumerWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.hook(MetricsTrailConsumer.from(TEST_CONSUMER, this.consumer)));
    }

    @Test
    public void hookNullConsumer() {
        MetricsTrail.begin();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrail.hook(null));
    }

    @Test
    public void commitForcedMetricWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.commit(new Metric()));
    }

    @Test
    public void commitUnforcedMetricWithoutExisting() {
        MetricsTrail.commit(new Metric(), false);
    }

    @Test
    public void commitNullMetric() {
        MetricsTrail.begin();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrail.commit(null));
    }

    @Test
    public void hasGatedWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.hasGated());
    }

    @Test
    public void isDeliveringWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.isDelivering());
    }

    @Test
    public void endWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrail.end());
    }
}
