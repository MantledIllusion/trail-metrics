package com.mantledillusion.metrics.trail.api.web;

import com.mantledillusion.metrics.trail.api.MeasurementType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Represents an attribute of a {@link WebMetric}.
 */
@XmlRootElement
@XmlType(propOrder = { "key", "value" })
public class WebMetricAttribute {

	@XmlElement(required = true, nillable = false)
	private String key;
	@XmlElement(required = true, nillable = false)
	private String value;
	@XmlElement(required = true, nillable = false)
	private MeasurementType type;

	/**
	 * Default constructor.
	 */
	public WebMetricAttribute() {
	}

	/**
	 * Pre-setting constructor.
	 * 
	 * @param key
	 *            The key of the attribute; might be null.
	 * @param value
	 *            The value of the attribute; might be null.
	 * @param type
	 * 			  The type of the attribute's value; might be null.
	 */
	public WebMetricAttribute(String key, String value, MeasurementType type) {
		this.key = key;
		this.value = value;
		this.type = type;
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
	 * Returns the type of the attribute's value.
	 *
	 * @return The type, might be null
	 */
	public MeasurementType getType() {
		return type;
	}

	/**
	 * Sets the type of the attribute's value.
	 *
	 * @param type The type to set, might be null.
	 */
	public void setType(MeasurementType type) {
		this.type = type;
	}
}
