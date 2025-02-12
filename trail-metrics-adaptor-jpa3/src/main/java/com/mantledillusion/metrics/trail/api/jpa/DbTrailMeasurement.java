package com.mantledillusion.metrics.trail.api.jpa;

import com.mantledillusion.metrics.trail.api.MeasurementType;

import jakarta.persistence.*;

/**
 * Represents an attribute of a {@link DbTrailEvent}.
 */
@Entity
@Table(name = "trail_measurement", indexes = {
		@Index(name = "IDX_ATTRIBUTE_KEY", columnList = "measurement_key"),
		@Index(name = "IDX_ATTRIBUTE_VALUE", columnList = "measurement_value"),
		@Index(name = "IDX_ATTRIBUTE_TYPE", columnList = "measurement_type"),
		@Index(name = "UIDX_MEASUREMENT_EVENT_ID_MEASUREMENT_KEY", columnList = "trail_event_id, measurement_key", unique = true)})
public class DbTrailMeasurement {

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "trail_event_id", nullable = false, foreignKey = @ForeignKey(name = "FK_MEASUREMENT_TO_EVENT",
			foreignKeyDefinition = "FOREIGN KEY (trail_event_id) REFERENCES trail_event (id) ON UPDATE CASCADE ON DELETE CASCADE"))
	private DbTrailEvent event;

	@Column(name = "measurement_key", length = 255, nullable = false)
	private String key;

	@Column(name = "measurement_value", length = 2047)
	private String value;

	@Column(name = "measurement_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private MeasurementType type;

	/**
	 * Default constructor.
	 */
	public DbTrailMeasurement() {
	}

	/**
	 * Pre-setting constructor.
	 *
	 * @param event
	 *            The metric this attribute belongs to; might be null.
	 * @param key
	 *            The key of the attribute; might be null.
	 * @param value
	 *            The value of the attribute; might be null.
	 * @param type
	 * 			  The type of the attribute's value; might be null.
	 */
	public DbTrailMeasurement(DbTrailEvent event, String key, String value, MeasurementType type) {
		this.event = event;
		this.key = key;
		this.value = value;
		this.type = type;
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
	public DbTrailEvent getEvent() {
		return event;
	}

	/**
	 * Sets the metric this attribute belongs to.
	 *
	 * @param event
	 *            The metric; might be null.
	 */
	public void setEvent(DbTrailEvent event) {
		this.event = event;
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
