package com.mantledillusion.metrics.trail.api.web;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single metric.
 */
@XmlRootElement
@XmlType(propOrder = { "identifier", "timestamp", "attributes" })
public class WebMetric {

	@XmlElement(required = true, nillable = false)
	private String identifier;
	@XmlElement(required = true, nillable = false)
	private ZonedDateTime timestamp = ZonedDateTime.now();
	@XmlElement(required = false, nillable = false)
	private List<WebMetricAttribute> attributes = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public WebMetric() {
	}

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public WebMetric(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link WebMetric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link WebMetric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @return The identifier, might be null
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link WebMetric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link WebMetric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the timestamp at which this {@link WebMetric} was created.
	 * <p>
	 * By default, this is set to {@link ZonedDateTime#now()}.
	 * 
	 * @return The timestamp, might be null
	 */
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link WebMetric} was created.
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
	 * Returns the list of this {@link WebMetric}'s {@link WebMetricAttribute}.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<WebMetricAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the list of this {@link WebMetric}'s {@link WebMetricAttribute}.
	 * 
	 * @param attributes
	 *            The attribute list; might be null.
	 */
	public void setAttributes(List<WebMetricAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Maps this {@link WebMetric} to a {@link Event}.
	 *
	 * @return A new {@link Event} instance, never null
	 */
	public Event to() {
		Event target = new Event(this.identifier);
		target.setTimestamp(this.timestamp);

		if (this.attributes != null) {
			target.setMeasurements(this.attributes
					.parallelStream()
					.map(attribute -> new Measurement(attribute.getKey(), attribute.getValue(), attribute.getType()))
					.collect(Collectors.toList()));
		}

		return target;
	}

	/**
	 * Maps the given {@link Event} to a {@link WebMetric}.
	 *
	 * @param source The metric to map from; might <b>not</b> be null.
	 * @return A new {@link WebMetric} instance, never null
	 */
	public static WebMetric from(Event source) {
		if (source == null) {
			throw new IllegalArgumentException("Cannot map a null metric");
		}
		WebMetric target = new WebMetric(source.getIdentifier());
		target.setTimestamp(source.getTimestamp());

		if (source.getMeasurements() != null) {
			target.setAttributes(source
					.getMeasurements()
					.parallelStream()
					.map(attribute -> new WebMetricAttribute(attribute.getKey(), attribute.getValue(), attribute.getType()))
					.collect(Collectors.toList()));
		}

		return target;
	}
}
