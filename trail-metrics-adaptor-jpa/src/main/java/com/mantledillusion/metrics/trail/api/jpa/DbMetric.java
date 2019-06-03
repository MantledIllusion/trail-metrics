package com.mantledillusion.metrics.trail.api.jpa;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single metric.
 */
public class DbMetric {

	private String identifier;
	private DbMetricType type;
	private ZonedDateTime timestamp = ZonedDateTime.now();
	private List<DbMetricAttribute> attributes = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public DbMetric() {}

	/**
	 * Constructor.
	 *
	 * @param identifier
	 *            The identifier; might be null.
	 * @param type
	 *            The type; might be null.
	 */
	public DbMetric(String identifier, DbMetricType type) {
		this.identifier = identifier;
		this.type = type;
	}

	/**
	 * Returns the identifier that allows categorizing {@link DbMetric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link DbMetric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @return The identifier, might be null
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link DbMetric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link DbMetric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the {@link DbMetricType} that specifies how this {@link DbMetric} will be
	 * interpreted.
	 * 
	 * @return The type, might be null.
	 */
	public DbMetricType getType() {
		return type;
	}

	/**
	 * Returns the {@link DbMetricType} that specifies how this {@link DbMetric} will be
	 * interpreted.
	 * 
	 * @param type
	 *            The type; might be null.
	 */
	public void setType(DbMetricType type) {
		this.type = type;
	}

	/**
	 * Returns the timestamp at which this {@link DbMetric} was created.
	 * <p>
	 * By default, this is set to {@link ZonedDateTime#now()}.
	 * 
	 * @return The timestamp, might be null
	 */
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link DbMetric} was created.
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
	 * Returns the list of this {@link DbMetric}'s {@link DbMetricAttribute}.
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link DbMetricType}
	 * requirements by specifying a {@link com.mantledillusion.metrics.trail.api.Metric#OPERATOR_ATTRIBUTE_KEY}
	 * attribute or to deliver meta information.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<DbMetricAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the list of this {@link DbMetric}'s {@link DbMetricAttribute}.
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link DbMetricType}
	 * requirements by specifying a {@link com.mantledillusion.metrics.trail.api.Metric#OPERATOR_ATTRIBUTE_KEY}
	 * attribute or to deliver meta information.
	 * 
	 * @param attributes
	 *            The attribute list; might be null.
	 */
	public void setAttributes(List<DbMetricAttribute> attributes) {
		this.attributes = attributes;
	}
}
