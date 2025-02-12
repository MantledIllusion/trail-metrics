package com.mantledillusion.metrics.trail.api.web;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a trail whose context a {@link WebMetricConsumer} received at least 1 {@link WebMetric}.
 */
@XmlRootElement
@XmlType(propOrder = { "correlationId, metrics" })
public class WebMetricTrail {

	@XmlElement(required = true, nillable = false)
	private String correlationId;
	@XmlElement(required = false, nillable = false)
	private List<WebMetric> metrics = new ArrayList<>();

	/**
	 * Default Constructor.
	 */
	public WebMetricTrail() {
	}

	/**
	 * Constructor.
	 * 
	 * @param correlationId
	 *            The id; might be null.
	 */
	public WebMetricTrail(String correlationId) {
		this.correlationId = correlationId;
	}

	/**
	 * Constructor.
	 * 
	 * @param correlationId
	 *            The id; might be null.
	 * @param metric
	 *            The first metric; might be null.
	 */
	public WebMetricTrail(String correlationId, WebMetric metric) {
		this.correlationId = correlationId;
		if (metric != null) {
			this.metrics.add(metric);
		}
	}

	/**
	 * Returns the session's ID.
	 * 
	 * @return The id, might be null
	 */
	public String getCorrelationId() {
		return correlationId;
	}

	/**
	 * Sets the session's ID.
	 * 
	 * @param correlationId
	 *            The id; might be null.
	 */
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	/**
	 * Returns the {@link WebMetric}s the consumer needs to receive from this session.
	 * 
	 * @return The {@link WebMetric} list, might be null
	 */
	public List<WebMetric> getMetrics() {
		return metrics;
	}

	/**
	 * Sets the {@link WebMetric}s the consumer needs to receive from this session.
	 * 
	 * @param metrics
	 *            The {@link WebMetric} list; might be null.
	 */
	public void setMetrics(List<WebMetric> metrics) {
		this.metrics = metrics;
	}
}
