package com.mantledillusion.metrics.trail.api;

import java.util.Objects;

/**
 * Represents a measurement of an {@link Event}.
 */
public class Measurement {

	private String key;
	private String value;
	private MeasurementType type;

	/**
	 * Default constructor.
	 */
	public Measurement() {
	}

	/**
	 * Pre-setting constructor.
	 * 
	 * @param key
	 *            The key of the measurement; might be null.
	 * @param value
	 *            The value of the measurement; might be null.
	 * @param type
	 * 			  The type of the measurement's value; might be null.
	 */
	public Measurement(String key, String value, MeasurementType type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}

	/**
	 * Returns the key of the measurement.
	 * 
	 * @return The key, might be null
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key of the measurement.
	 * 
	 * @param key
	 *            The key to set; might be null.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the value of the measurement.
	 * 
	 * @return The value, might be null
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the measurement.
	 * 
	 * @param value
	 *            The value to set, might be null.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the type of the measurement's value.
	 *
	 * @return The type, might be null
	 */
	public MeasurementType getType() {
		return type;
	}

	/**
	 * Sets the type of the measurement's value.
	 *
	 * @param type The type to set, might be null.
	 */
	public void setType(MeasurementType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Measurement that = (Measurement) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(value, that.value) &&
				Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value, type);
	}

	@Override
	public String toString() {
		return this.key + "=" + this.value;
	}

	/**
	 * Parses this measurement's value
	 *
	 * @param <T> The measurement value's type.
	 * @return The parsed value
	 */
	public <T> T parseValue() {
		if (this.type == null) {
			throw new IllegalStateException("Cannot parse a typeless measurement");
		}
		return (T) this.type.parse(this.value);
	}
}
