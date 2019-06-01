package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricsPredicateTest {

    @Test
    public void testNullAndConjunction() {
        MetricsPredicate predicate = metric -> true;
        Assertions.assertThrows(IllegalArgumentException.class, () -> predicate.and(null));
    }

    @Test
    public void testAndConjunction() {
        MetricsPredicate predicateA = metric -> metric.getIdentifier().startsWith("A");
        MetricsPredicate predicateB = metric -> metric.getIdentifier().endsWith("B");

        MetricsPredicate conjunction = predicateA.and(predicateB);
        Assertions.assertFalse(conjunction.test(new Metric("X---X", null)));
        Assertions.assertFalse(conjunction.test(new Metric("A---X", null)));
        Assertions.assertFalse(conjunction.test(new Metric("X---B", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---B", null)));

        conjunction = conjunction.functionalClone();
        Assertions.assertFalse(conjunction.test(new Metric("X---X", null)));
        Assertions.assertFalse(conjunction.test(new Metric("A---X", null)));
        Assertions.assertFalse(conjunction.test(new Metric("X---B", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---B", null)));
    }

    @Test
    public void testNullOrConjunction() {
        MetricsPredicate predicate = metric -> true;
        Assertions.assertThrows(IllegalArgumentException.class, () -> predicate.or(null));
    }

    @Test
    public void testOrConjunction() {
        MetricsPredicate predicateA = metric -> metric.getIdentifier().startsWith("A");
        MetricsPredicate predicateB = metric -> metric.getIdentifier().endsWith("B");

        MetricsPredicate conjunction = predicateA.or(predicateB);
        Assertions.assertFalse(conjunction.test(new Metric("X---X", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---X", null)));
        Assertions.assertTrue(conjunction.test(new Metric("X---B", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---B", null)));

        conjunction = conjunction.functionalClone();
        Assertions.assertFalse(conjunction.test(new Metric("X---X", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---X", null)));
        Assertions.assertTrue(conjunction.test(new Metric("X---B", null)));
        Assertions.assertTrue(conjunction.test(new Metric("A---B", null)));
    }

    @Test
    public void testValveFromNullPredicate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsValve.of(null));
    }

    @Test
    public void testValveState() {
        MetricsPredicate predicate = metric -> metric.getType() == MetricType.PHASE;
        MetricsValve valve = predicate.asValve();

        Assertions.assertFalse(valve.isOpen());
        Assertions.assertFalse(valve.test(new Metric(null, MetricType.ALERT, null)));
        Assertions.assertFalse(valve.isOpen());
        Assertions.assertTrue(valve.test(new Metric(null, MetricType.PHASE, null)));
        Assertions.assertTrue(valve.isOpen());
        Assertions.assertTrue(valve.test(new Metric(null, MetricType.ALERT, null)));
        Assertions.assertTrue(valve.isOpen());
    }
}
