package com.mantledillusion.metrics.trail.api.jpa;

import com.mantledillusion.metrics.trail.ZoneIdStringAttributeConverter;
import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single metric.
 */
@Entity
@Table(name = "metric")
public class DbMetric {

	@Id
	@Column(name = "identifier", updatable = false, nullable = false)
	private String identifier;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private MetricType type;

	@Column(name = "timestamp", nullable = false)
	private LocalDateTime timestamp;

	@Column(name = "timezone", nullable = false)
	@Convert(converter = ZoneIdStringAttributeConverter.class)
	private ZoneId timezone;

	@OneToMany(mappedBy = "metric_identifier")
	private List<DbMetricAttribute> attributes;

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
	public DbMetric(String identifier, MetricType type) {
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
	 * Returns the {@link MetricType} that specifies how this {@link DbMetric} will be
	 * interpreted.
	 * 
	 * @return The type, might be null.
	 */
	public MetricType getType() {
		return type;
	}

	/**
	 * Returns the {@link MetricType} that specifies how this {@link DbMetric} will be
	 * interpreted.
	 * 
	 * @param type
	 *            The type; might be null.
	 */
	public void setType(MetricType type) {
		this.type = type;
	}

	/**
	 * Returns the timestamp at which this {@link DbMetric} was created, without time zone.
	 * 
	 * @return The timestamp, might be null
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link DbMetric} was created, without time zone.
	 * 
	 * @param timestamp
	 *            The timestamp, might be null.
	 */
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the timezone this {@link DbMetric} was created in.
	 *
	 * @return The timezone, might be null
	 */
	public ZoneId getTimezone() {
		return timezone;
	}

	/**
	 * Sets the timezone this {@link DbMetric} was created in.
	 *
	 * @param timezone
	 * 			The timezone, might be null.
	 */
	public void setTimezone(ZoneId timezone) {
		this.timezone = timezone;
	}

	/**
	 * Returns the list of this {@link DbMetric}'s {@link DbMetricAttribute}.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<DbMetricAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the list of this {@link DbMetric}'s {@link DbMetricAttribute}.
	 * 
	 * @param attributes
	 *            The attribute list; might be null.
	 */
	public void setAttributes(List<DbMetricAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Maps this {@link DbMetric} to a {@link Metric}.
	 *
	 * @return A new {@link Metric} instance, never null
	 */
	public Metric to() {
		Metric target = new Metric(this.identifier, this.type);
		ZonedDateTime timestamp = this.timestamp != null && this.timezone != null ?
				ZonedDateTime.of(this.timestamp, this.timezone) : null;
		target.setTimestamp(timestamp);

		if (this.attributes != null) {
			target.setAttributes(this.attributes
					.parallelStream()
					.map(attribute -> new MetricAttribute(attribute.getKey(), attribute.getValue()))
					.collect(Collectors.toList()));
		}

		return target;
	}

	/**
	 * Maps the given {@link Metric} to a {@link DbMetric}.
	 *
	 * @param source The metric to map from; might <b>not</b> be null.
	 * @return A new {@link DbMetric} instance, never null
	 */
	public static DbMetric from(Metric source) {
		if (source == null) {
			throw new IllegalArgumentException("Cannot map a null metric");
		}
		DbMetric target = new DbMetric(source.getIdentifier(), source.getType());
		if (source.getTimestamp() != null) {
			target.setTimestamp(source.getTimestamp().toLocalDateTime());
			target.setTimezone(source.getTimestamp().getZone());
		}

		if (source.getAttributes() != null) {
			target.setAttributes(source
					.getAttributes()
					.parallelStream()
					.map(attribute -> new DbMetricAttribute(target, attribute.getKey(), attribute.getValue()))
					.collect(Collectors.toList()));
		}

		return target;
	}
}
