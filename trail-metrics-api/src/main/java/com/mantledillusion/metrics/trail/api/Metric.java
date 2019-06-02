package com.mantledillusion.metrics.trail.api;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mantledillusion.metrics.trail.MetricValidator;

/**
 * Represents a single metric.
 * <p>
 * This is a POJO type that allows setting its fields freely, but instances of
 * it can only be dispatched when {@link MetricValidator#validate(Metric)}
 * evaluates {@code true} for it.
 */
public class Metric {

	public static final String OPERATOR_ATTRIBUTE_KEY = "_operator";

	private String identifier;
	private MetricType type;
	private ZonedDateTime timestamp = ZonedDateTime.now();
	private List<MetricAttribute> attributes = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public Metric() {
		this(null, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 * @param type
	 *            The type; might be null.
	 */
	public Metric(String identifier, MetricType type) {
		this(identifier, type, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 * @param type
	 *            The type; might be null.
	 * @param operator
	 *            The operator value to add as a {@link MetricAttribute} with
	 *            {@link #OPERATOR_ATTRIBUTE_KEY} for the {@link MetricType}'s
	 *            operator; might be null.
	 */
	public Metric(String identifier, MetricType type, Object operator) {
		this.identifier = identifier;
		this.type = type;
		this.attributes.add(MetricAttribute.operatorOf(operator));
	}

	/**
	 * Returns the identifier that allows categorizing {@link Metric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link Metric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @return The identifier, might be null
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the identifier that allows categorizing {@link Metric}s.
	 * <p>
	 * For categorization it is reasonable to follow a naming scheme for
	 * {@link Metric} identifiers like "this.is.some.event.identifier".
	 * 
	 * @param identifier
	 *            The identifier; might be null.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the {@link MetricType} that specifies how this {@link Metric} will be
	 * interpreted.
	 * 
	 * @return The type, might be null.
	 */
	public MetricType getType() {
		return type;
	}

	/**
	 * Returns the {@link MetricType} that specifies how this {@link Metric} will be
	 * interpreted.
	 * 
	 * @param type
	 *            The type; might be null.
	 */
	public void setType(MetricType type) {
		this.type = type;
	}

	/**
	 * Returns the timestamp at which this {@link Metric} was created.
	 * <p>
	 * By default, this is set to {@link ZonedDateTime#now()}.
	 * 
	 * @return The timestamp, might be null
	 */
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp at which this {@link Metric} was created.
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
	 * Returns the list of this {@link Metric}'s {@link MetricAttribute}.
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link MetricType}
	 * requirements by specifying a {@link #OPERATOR_ATTRIBUTE_KEY} attribute or to
	 * deliver meta information.
	 * 
	 * @return The attribute list, might be null
	 */
	public List<MetricAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the list of this {@link Metric}'s {@link MetricAttribute}.
	 * <p>
	 * Attributes can be attached to a metric to fulfill {@link MetricType}
	 * requirements by specifying a {@link #OPERATOR_ATTRIBUTE_KEY} attribute or to
	 * deliver meta information.
	 * 
	 * @param attributes
	 *            The attribute list; might be null.
	 */
	public void setAttributes(List<MetricAttribute> attributes) {
		this.attributes = attributes;
	}
}
