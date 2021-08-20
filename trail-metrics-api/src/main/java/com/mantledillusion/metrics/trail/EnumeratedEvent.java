package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;

import java.util.Arrays;

/**
 * Interface for {@link Enum}s of {@link Event} types for a specific domain.
 */
public interface EnumeratedEvent {

    /** Declares the {@link Enum#name()} method.
     *
     * @return The name of the {@link Enum} entry, never null
     */
    String name();

    /**
     * Returns a prefix all of the enumerated metrics share.
     * <p>
     * Empty string by default.
     *
     * @return A prefix, never null, might be empty
     */
    default String getPrefix() {
        return "";
    }

    /**
     * Generates a metric ID from an {@link Enum} value.
     * <p>
     * The value's {@link Enum#name()} is expected to be separated by the '_' char.
     *
     * @return A fitting metric ID, never null
     */
    default String getIdentifier() {
        return getPrefix() + '.' + name().toLowerCase().replace('_', '.');
    }

    /**
     * Creates a new {@link Event} of this type.
     *
     * @param measurements The attributes of the event; might be null or contain nulls.
     * @return A new {@link Event} instance, never null
     */
    default Event build(Measurement... measurements) {
        Event event = new Event(getIdentifier());
        Arrays.stream(measurements).forEach(attribute -> {
            if (attribute != null)
                event.getMeasurements().add(attribute);
        });
        return event;
    }
}
