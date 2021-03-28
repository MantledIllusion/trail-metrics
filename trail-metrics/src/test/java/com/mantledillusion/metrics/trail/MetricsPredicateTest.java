package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
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
        Assertions.assertFalse(conjunction.test(new Event("X---X")));
        Assertions.assertFalse(conjunction.test(new Event("A---X")));
        Assertions.assertFalse(conjunction.test(new Event("X---B")));
        Assertions.assertTrue(conjunction.test(new Event("A---B")));

        conjunction = conjunction.functionalClone();
        Assertions.assertFalse(conjunction.test(new Event("X---X")));
        Assertions.assertFalse(conjunction.test(new Event("A---X")));
        Assertions.assertFalse(conjunction.test(new Event("X---B")));
        Assertions.assertTrue(conjunction.test(new Event("A---B")));
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
        Assertions.assertFalse(conjunction.test(new Event("X---X")));
        Assertions.assertTrue(conjunction.test(new Event("A---X")));
        Assertions.assertTrue(conjunction.test(new Event("X---B")));
        Assertions.assertTrue(conjunction.test(new Event("A---B")));

        conjunction = conjunction.functionalClone();
        Assertions.assertFalse(conjunction.test(new Event("X---X")));
        Assertions.assertTrue(conjunction.test(new Event("A---X")));
        Assertions.assertTrue(conjunction.test(new Event("X---B")));
        Assertions.assertTrue(conjunction.test(new Event("A---B")));
    }

    @Test
    public void testValveFromNullPredicate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MetricsValve.of(null));
    }

    @Test
    public void testValveState() {
        MetricsPredicate predicate = metric -> metric.getIdentifier().equals("open");
        MetricsValve valve = predicate.asValve();

        Assertions.assertFalse(valve.isOpen());
        Assertions.assertFalse(valve.test(new Event("close")));
        Assertions.assertFalse(valve.isOpen());
        Assertions.assertTrue(valve.test(new Event("open")));
        Assertions.assertTrue(valve.isOpen());
        Assertions.assertTrue(valve.test(new Event("close")));
        Assertions.assertTrue(valve.isOpen());
    }
}
