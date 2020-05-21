package com.mantledillusion.metrics.trail.api.jpa;

import javax.persistence.*;

/**
 * Represents an attribute of a {@link DbMetric}.
 */
@Entity
@Table(name = "metric_attribute", indexes = {
		@Index(name = "IDX_ATTRIBUTE_KEY", columnList = "attribute_key"),
		@Index(name = "IDX_ATTRIBUTE_VALUE", columnList = "attribute_value"),
		@Index(name = "UIDX_METRIC_ATTRIBUTE_METRIC_ID_ATTRIBUTE_KEY", columnList = "metric_id, attribute_key", unique = true)})
public class DbMetricAttribute {

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "metric_id")
	private DbMetric metric;

	@Column(name = "attribute_key", length = 255, nullable = false)
	private String key;

	@Column(name = "attribute_value", length = 255)
	private String value;

	/**
	 * Default constructor.
	 */
	public DbMetricAttribute() {
	}

	/**
	 * Pre-setting constructor.
	 *
	 * @param metric
	 *            The metric this attribute belongs to; might be null.
	 * @param key
	 *            The key of the attribute; might be null.
	 * @param value
	 *            The value of the attribute; might be null.
	 */
	public DbMetricAttribute(DbMetric metric, String key, String value) {
		this.metric = metric;
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the ID of this attribute
	 *
	 * @return This attribute's ID, might be null
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the ID of this attribute
	 *
	 * @param id
	 *            The attribute's ID; might be null.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the metric this attribute belongs to.
	 *
	 * @return The metric, might be null
	 */
	public DbMetric getMetric() {
		return metric;
	}

	/**
	 * Sets the metric this attribute belongs to.
	 *
	 * @param metric
	 *            The metric; might be null.
	 */
	public void setMetric(DbMetric metric) {
		this.metric = metric;
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
