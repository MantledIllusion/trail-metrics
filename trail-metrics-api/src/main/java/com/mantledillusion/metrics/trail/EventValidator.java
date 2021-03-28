package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;

import java.util.Objects;

/**
 * Validator for {@link Event} instances.
 */
public class EventValidator {

	private EventValidator() {
	}

	/**
	 * Validates that the given {@link Event} is valid to be dispatched:<br>
	 * - Has to have a timestamp<br>
	 * - Has to have a non-empty identifier<br>
	 * - Has to have a type<br>
	 * - If it has measurements, they all have to be non-null with non-empty key and non-null type<br>
	 * 
	 * @param event The event to validate; might be null.
	 * @throws IllegalArgumentException If the event is invalid in any way.
	 */
	public static void validate(Event event) throws IllegalArgumentException {
		if (event == null) {
			throw new IllegalArgumentException("Cannot dispatch a null event");
		} else if (event.getTimestamp() == null) {
			throw new IllegalArgumentException("Cannot dispatch a timestampless event");
		} else if (event.getIdentifier() == null || event.getIdentifier().isEmpty()) {
			throw new IllegalArgumentException("Cannot dispatch an identifierless event");
		}

		if (event.getMeasurements() != null) {
			if (event.getMeasurements().stream().anyMatch(Objects::isNull)) {
				throw new IllegalArgumentException("Cannot dispatch an event with an attribute that is null");
			} else if (event.getMeasurements().stream().anyMatch(attr -> attr.getKey() == null)) {
				throw new IllegalArgumentException("Cannot dispatch an event with an attribute whose key is null");
			}

			event.getMeasurements().stream()
					.filter(attr -> attr.getType() == null)
					.findFirst()
					.ifPresent(attr -> {
						throw new IllegalArgumentException(String.format("Cannot dispatch an event with attribute %s whose type is null", attr.getKey()));
					});

			event.getMeasurements().stream()
					.filter(attr -> !attr.getType().valid(attr.getValue()))
					.findFirst()
					.ifPresent(attr -> {
						throw new IllegalArgumentException(String.format("Cannot dispatch an event with attribute %s whose value '%s' is invalid for its type %s", attr.getKey(), attr.getValue(), attr.getType()));
					});
		}
	}
}
