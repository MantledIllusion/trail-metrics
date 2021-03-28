package com.mantledillusion.metrics.trail.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mantledillusion.metrics.trail.EventValidator;

/**
 * Represents the occurrence of a set of measurements on a trail, or in other words: single metric.
 * <p>
 * This is a POJO type that allows setting its fields freely, but instances of it can only be dispatched when
 * {@link EventValidator#validate(Event)} evaluates {@code true} for it.
 */
public class Event {

	private String identifier;
	private ZonedDateTime timestamp = ZonedDateTime.now();
	private List<Measurement> measurements = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public Event() {}

	/**
	 * Constructor.
	 *
	 * @param identifier
	 *            The identifier; might be null.
	 * @param measurements
	 * 			  additional Attributes; might not contain nulls.
	 */
	public Event(String identifier, Measurement... measurements) {
		this.identifier = identifier;
		this.measurements = new ArrayList<>(Arrays.asList(measurements));
	}

	/**
	 * Returns the identifier that allows categorizing {@link Event}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link Event} identifiers like "this.is.some.event.identifier".
	 *
	 * @return The identifier, might be null
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link Event}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link Event} identifiers like "this.is.some.event.identifier".
	 *
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the timestamp at which this {@link Event} was created.
	 * <p>
	 * By default, this is set to {@link ZonedDateTime#now()}.
	 *
	 * @return The timestamp, might be null
	 */
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link Event} was created.
	 * <p>
	 * By default, this is set to {@link ZonedDateTime#now()}.
	 *
	 * @param timestamp
	 *            The timestamp, might be null.
	 */
	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the list of this {@link Event}'s {@link Measurement}.
	 *
	 * @return The attribute list, might be null
	 */
	public List<Measurement> getMeasurements() {
		return measurements;
	}

	/**
	 * Returns the list of this {@link Event}'s {@link Measurement}.
	 *
	 * @param measurements
	 *            The attribute list; might be null.
	 */
	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Event event = (Event) o;
		return Objects.equals(identifier, event.identifier) &&
				Objects.equals(timestamp, event.timestamp) &&
				Objects.equals(measurements, event.measurements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, timestamp, measurements);
	}

	@Override
	public String toString() {
		return this.identifier + " (" + this.timestamp + ")";
	}
}
