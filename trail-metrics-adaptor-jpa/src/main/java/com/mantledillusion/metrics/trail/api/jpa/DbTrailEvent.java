package com.mantledillusion.metrics.trail.api.jpa;

import com.mantledillusion.metrics.trail.ZoneIdStringAttributeConverter;
import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single metric.
 */
@Entity
@Table(name = "trail_event", indexes = {
		@Index(name = "IDX_METRIC_IDENTIFIER", columnList = "identifier")})
public class DbTrailEvent {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "trail_consumer_id", nullable = false, foreignKey = @ForeignKey(name = "FK_EVENT_TO_TRAIL",
			foreignKeyDefinition = "FOREIGN KEY (trail_consumer_id) REFERENCES trail_consumer (id) ON UPDATE CASCADE ON DELETE CASCADE"))
	private DbTrailConsumer trail;

	@Column(name = "identifier", length = 255, nullable = false)
	private String identifier;

	@Column(name = "time_stamp", nullable = false)
	private LocalDateTime timestamp;

	@Column(name = "time_zone", nullable = false)
	@Convert(converter = ZoneIdStringAttributeConverter.class)
	private ZoneId timezone;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DbTrailMeasurement> measurements;

	/**
	 * Default constructor.
	 */
	public DbTrailEvent() {}

	/**
	 * Constructor.
	 *
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public DbTrailEvent(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the database ID of this metric.
	 *
	 * @return The ID, may be null if the metric is not persisted yet
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the database ID of this metric.
	 *
	 * @param id The ID to set; might be null if this a new metric rather than an existing one in need to be updated.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public DbTrailConsumer getTrail() {
		return trail;
	}

	public void setTrail(DbTrailConsumer trail) {
		this.trail = trail;
	}

	/**
	 * Returns the identifier that allows categorizing {@link DbTrailEvent}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link DbTrailEvent} identifiers like "this.is.some.event.identifier".
	 * 
	 * @return The identifier, might be null
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link DbTrailEvent}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link DbTrailEvent} identifiers like "this.is.some.event.identifier".
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the timestamp at which this {@link DbTrailEvent} was created, without time zone.
	 * 
	 * @return The timestamp, might be null
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link DbTrailEvent} was created, without time zone.
	 * 
	 * @param timestamp
	 *            The timestamp, might be null.
	 */
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the timezone this {@link DbTrailEvent} was created in.
	 *
	 * @return The timezone, might be null
	 */
	public ZoneId getTimezone() {
		return timezone;
	}

	/**
	 * Sets the timezone this {@link DbTrailEvent} was created in.
	 *
	 * @param timezone
	 * 			The timezone, might be null.
	 */
	public void setTimezone(ZoneId timezone) {
		this.timezone = timezone;
	}

	/**
	 * Returns the list of this {@link DbTrailEvent}'s {@link DbTrailMeasurement}.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<DbTrailMeasurement> getMeasurements() {
		return measurements;
	}

	/**
	 * Returns the list of this {@link DbTrailEvent}'s {@link DbTrailMeasurement}.
	 * 
	 * @param measurements
	 *            The attribute list; might be null.
	 */
	public void setMeasurements(List<DbTrailMeasurement> measurements) {
		this.measurements = measurements;
	}

	/**
	 * Maps this {@link DbTrailEvent} to a {@link Event}.
	 *
	 * @return A new {@link Event} instance, never null
	 */
	public Event to() {
		Event target = new Event(this.identifier);
		ZonedDateTime timestamp = this.timestamp != null && this.timezone != null ?
				ZonedDateTime.of(this.timestamp, this.timezone) : null;
		target.setTimestamp(timestamp);

		if (this.measurements != null) {
			target.setMeasurements(this.measurements
					.parallelStream()
					.map(attribute -> new Measurement(attribute.getKey(), attribute.getValue(), attribute.getType()))
					.collect(Collectors.toList()));
		}

		return target;
	}

	/**
	 * Maps the given {@link Event} to a {@link DbTrailEvent}.
	 *
	 * @param source The metric to map from; might <b>not</b> be null.
	 * @return A new {@link DbTrailEvent} instance, never null
	 */
	public static DbTrailEvent from(Event source) {
		if (source == null) {
			throw new IllegalArgumentException("Cannot map a null metric");
		}
		DbTrailEvent target = new DbTrailEvent(source.getIdentifier());
		if (source.getTimestamp() != null) {
			target.setTimestamp(source.getTimestamp().toLocalDateTime());
			target.setTimezone(source.getTimestamp().getZone());
		}

		if (source.getMeasurements() != null) {
			target.setMeasurements(source
					.getMeasurements()
					.parallelStream()
					.map(attribute -> new DbTrailMeasurement(target,
							attribute.getKey(),
							attribute.getValue() == null
									? null
									: attribute.getValue().substring(0, Math.min(attribute.getValue().length(), 2047)),
							attribute.getType()))
					.collect(Collectors.toList()));
		}

		return target;
	}
}
