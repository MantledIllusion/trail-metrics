package com.mantledillusion.metrics.trail.api;

/**
 * Represents an attribute of a {@link Metric}.
 */
public class MetricAttribute {

	private String key;
	private String value;

	/**
	 * Default constructor.
	 */
	public MetricAttribute() {
	}

	/**
	 * Pre-setting constructor.
	 * 
	 * @param key
	 *            The key of the attribute; might be null.
	 * @param value
	 *            The value of the attribute; might be null.
	 */
	public MetricAttribute(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the key of the attribute.
	 * 
	 * @return The key, might be null
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key of the attribute.
	 * 
	 * @param key
	 *            The key to set; might be null.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the value of the attribute.
	 * 
	 * @return The value, might be null
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the attribute.
	 * 
	 * @param value
	 *            The value to set, might be null.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Creates a new {@link MetricAttribute} that serves as operator for a {@link MetricType} that requires one.
	 *
	 * @param operator The value of the operator {@link MetricAttribute}; might be null
	 * @return A new {@link MetricAttribute}, never null
	 */
	public static MetricAttribute operatorOf(Object operator) {
		return new MetricAttribute(Metric.OPERATOR_ATTRIBUTE_KEY, operator == null ? null : String.valueOf(operator));
	}
}
