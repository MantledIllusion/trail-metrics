package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MetricsTrailSupportTest {

    private static final UUID TRAIL_ID = UUID.randomUUID();

    @AfterEach
    public void endTrail() {
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }
    
    @Test
    public void beginRandomTrailTest() {
        Assertions.assertFalse(MetricsTrailSupport.has());

        MetricsTrailSupport.begin();

        Assertions.assertTrue(MetricsTrailSupport.has());
        Assertions.assertNotNull(MetricsTrailSupport.get());

        MetricsTrailSupport.end();

        Assertions.assertFalse(MetricsTrailSupport.has());
    }

    @Test
    public void beginSpecificTrailTest() {
        Assertions.assertFalse(MetricsTrailSupport.has());

        MetricsTrailSupport.begin(TRAIL_ID);

        Assertions.assertTrue(MetricsTrailSupport.has());
        Assertions.assertEquals(TRAIL_ID, MetricsTrailSupport.get());

        MetricsTrailSupport.end();

        Assertions.assertFalse(MetricsTrailSupport.has());
    }

    @Test
    public void beginTrailDuringExisting() {
        MetricsTrailSupport.begin();
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.begin());
    }

    @Test
    public void beginTrailWithoutId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrailSupport.begin(null));
    }

    @Test
    public void getTrailWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.get());
    }

    @Test
    public void hookConsumerWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.hook(null));
    }

    @Test
    public void hookNullConsumer() {
        MetricsTrailSupport.begin();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrailSupport.hook(null));
    }

    @Test
    public void commitForcedMetricWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.commit(new Metric()));
    }

    @Test
    public void commitUnforcedMetricWithoutExisting() {
        MetricsTrailSupport.commit(new Metric(), false);
    }

    @Test
    public void commitNullMetric() {
        MetricsTrailSupport.begin();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsTrailSupport.commit(null));
    }

    @Test
    public void hasGatedWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.hasGated());
    }

    @Test
    public void isDeliveringWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.isDelivering());
    }

    @Test
    public void endWithoutExisting() {
        Assertions.assertThrows(IllegalStateException.class, () -> MetricsTrailSupport.end());
    }

    @Test
    public void listenTrail() {
        Map<UUID, MetricsTrailListener.EventType> trails = new HashMap<>();
        MetricsTrailListener listener = (trail, eventType) -> trails.put(trail.getTrailId(), eventType);

        MetricsTrailSupport.addListener(listener, MetricsTrailListener.ReferenceMode.HARD);
        UUID trailId = MetricsTrailSupport.begin();
        Assertions.assertTrue(trails.containsKey(trailId));
        Assertions.assertEquals(MetricsTrailListener.EventType.BEGIN, trails.get(trailId));
        MetricsTrailSupport.end();
        Assertions.assertEquals(MetricsTrailListener.EventType.END, trails.get(trailId));

        trailId = UUID.randomUUID();
        MetricsTrail trail = new MetricsTrail(trailId);
        MetricsTrailSupport.bind(trail);
        Assertions.assertTrue(trails.containsKey(trailId));
        Assertions.assertEquals(MetricsTrailListener.EventType.BIND, trails.get(trailId));
        Assertions.assertSame(trail, MetricsTrailSupport.release());
        Assertions.assertEquals(MetricsTrailListener.EventType.RELEASE, trails.get(trailId));

        MetricsTrailSupport.removeListener(listener);
        trailId = MetricsTrailSupport.begin();
        Assertions.assertFalse(trails.containsKey(trailId));
    }
}
