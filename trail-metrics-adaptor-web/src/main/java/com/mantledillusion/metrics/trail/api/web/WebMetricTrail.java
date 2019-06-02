package com.mantledillusion.metrics.trail.api.web;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a trail whose context a {@link WebMetricConsumer} received at least 1 {@link WebMetric}.
 */
@XmlRootElement
@XmlType(propOrder = { "trailId, metrics" })
public class WebMetricTrail {

	@XmlElement(required = true, nillable = false)
	private String trailId;
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
	 * @param trailId
	 *            The id; might be null.
	 */
	public WebMetricTrail(String trailId) {
		this.trailId = trailId;
	}

	/**
	 * Constructor.
	 * 
	 * @param trailId
	 *            The id; might be null.
	 * @param metric
	 *            The first metric; might be null.
	 */
	public WebMetricTrail(String trailId, WebMetric metric) {
		this.trailId = trailId;
		if (metric != null) {
			this.metrics.add(metric);
		}
	}

	/**
	 * Returns the session's ID.
	 * 
	 * @return The id, might be null
	 */
	public String getTrailId() {
		return trailId;
	}

	/**
	 * Sets the session's ID.
	 * 
	 * @param trailId
	 *            The id; might be null.
	 */
	public void setTrailId(String trailId) {
		this.trailId = trailId;
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
