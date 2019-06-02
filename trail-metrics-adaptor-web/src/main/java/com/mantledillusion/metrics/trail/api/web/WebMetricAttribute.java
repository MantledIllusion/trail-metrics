package com.mantledillusion.metrics.trail.api.web;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
	 */
	public WebMetricAttribute(String key, String value) {
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
}
