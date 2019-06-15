package com.mantledillusion.metrics.trail.api.web;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single metric.
 */
@XmlRootElement
@XmlType(propOrder = { "identifier", "type", "timestamp", "attributes" })
public class WebMetric {

	@XmlElement(required = true, nillable = false)
	private String identifier;
	@XmlElement(required = true, nillable = false)
	private WebMetricType type;
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
	 * @param type
	 *            The type; might be null.
	 */
	public WebMetric(String identifier, WebMetricType type) {
		this.identifier = identifier;
		this.type = type;
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
	 * Returns the {@link WebMetricType} that specifies how this {@link WebMetric} will be
	 * interpreted.
	 * 
	 * @return The type, might be null.
	 */
	public WebMetricType getType() {
		return type;
	}

	/**
	 * Returns the {@link WebMetricType} that specifies how this {@link WebMetric} will be
	 * interpreted.
	 * 
	 * @param type
	 *            The type; might be null.
	 */
	public void setType(WebMetricType type) {
		this.type = type;
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
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link WebMetricType}
	 * requirements by specifying a {@link Metric#OPERATOR_ATTRIBUTE_KEY} attribute or to
	 * deliver meta information.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<WebMetricAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the list of this {@link WebMetric}'s {@link WebMetricAttribute}.
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link WebMetricType}
	 * requirements by specifying a {@link Metric##OPERATOR_ATTRIBUTE_KEY} attribute or to
	 * deliver meta information.
	 * 
	 * @param attributes
	 *            The attribute list; might be null.
	 */
	public void setAttributes(List<WebMetricAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Maps this {@link WebMetric} to a {@link Metric}.
	 *
	 * @return A new {@link Metric} instance, never null
	 */
	public Metric to() {
		MetricType type = this.type != null ? MetricType.valueOf(this.type.name()) : null;
		Metric target = new Metric(this.identifier, type);
		target.setTimestamp(this.timestamp);

		if (this.attributes != null) {
			target.setAttributes(this.attributes
					.parallelStream()
					.map(attribute -> new MetricAttribute(attribute.getKey(), attribute.getValue()))
					.collect(Collectors.toList()));
		}

		return target;
	}

	/**
	 * Maps the given {@link Metric} to a {@link WebMetric}.
	 *
	 * @param source The metric to map from; might <b>not</b> be null.
	 * @return A new {@link WebMetric} instance, never null
	 */
	public static WebMetric from(Metric source) {
		if (source == null) {
			throw new IllegalArgumentException("Cannot map a null metric");
		}
		WebMetricType type = source.getType() != null ? WebMetricType.valueOf(source.getType().name()) : null;
		WebMetric target = new WebMetric(source.getIdentifier(), type);
		target.setTimestamp(source.getTimestamp());

		if (source.getAttributes() != null) {
			target.setAttributes(source
					.getAttributes()
					.parallelStream()
					.map(attribute -> new WebMetricAttribute(attribute.getKey(), attribute.getValue()))
					.collect(Collectors.toList()));
		}

		return target;
	}
}
