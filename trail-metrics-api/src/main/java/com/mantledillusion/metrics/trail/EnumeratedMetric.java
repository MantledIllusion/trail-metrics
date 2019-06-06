package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;

import java.util.Arrays;

/**
 * Interface for {@link Enum}s of {@link Metric} types for a specific domain.
 */
public interface EnumeratedMetric {

    /**
     * Generates a metric ID from an {@link Enum} value.
     * <p>
     * The value's {@link Enum#name()} is expected to be separated by the '_' char.
     *
     * @param prefix The prefix of the final metric ID; might <b>not</b> be null.
     * @param enumValue The {@link Enum} value to generate the metric ID for; might <b>not</b> be null.
     * @return A fitting metric ID, never null
     */
    default String generateMetricId(String prefix, Enum<?> enumValue) {
        if (prefix == null) {
            throw new IllegalArgumentException("The prefix cannot be null");
        } else if (enumValue == null) {
            throw new IllegalArgumentException("The enum value cannot be null");
        }
        return prefix + '.' + enumValue.name().toLowerCase().replace('_', '.');
    }

    /**
     * Returns the ID of the metric.
     *
     * @return The ID, never null
     */
    String getMetricId();


    /**
     * Returns the {@link MetricType} of the metric.
     *
     * @return The type, never null
     */
    MetricType getType();

    /**
     * Creates a new {@link Metric} of this type.
     *
     * @param attributes The attributes of the event; might be null or contain nulls.
     * @return A new {@link Metric} instance, never null
     */
    default Metric build(MetricAttribute... attributes) {
        Metric event = new Metric(getMetricId(), getType());
        Arrays.stream(attributes).forEach(attribute -> {
            if (attribute != null)
                event.getAttributes().add(attribute);
        });
        return event;
    }

    /**
     * Creates a new {@link Metric} of this type.
     *
     * @param operator The optional operator for the {@link MetricType} of this {@link EnumeratedMetric}; might <b>not</b> be null.
     * @param attributes The attributes of the event; might be null or contain nulls.
     * @return A new {@link Metric} instance, never null
     */
    default Metric build(String operator, MetricAttribute... attributes) {
        Metric event = new Metric(getMetricId(), getType(), operator);
        Arrays.stream(attributes).forEach(attribute -> {
            if (attribute != null)
                event.getAttributes().add(attribute);
        });
        return event;
    }

    /**
     * Creates a new {@link Metric} of this type.
     *
     * @param operand The operand required for the {@link MetricType} of this {@link EnumeratedMetric}.
     * @param attributes The attributes of the event; might be null or contain nulls.
     * @return A new {@link Metric} instance, never null
     */
    default Metric build(float operand, MetricAttribute... attributes) {
        Metric event = new Metric(getMetricId(), getType(), Float.toString(operand));
        Arrays.stream(attributes).forEach(attribute -> {
            if (attribute != null)
                event.getAttributes().add(attribute);
        });
        return event;
    }
}
