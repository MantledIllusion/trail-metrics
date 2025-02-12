package com.mantledillusion.metrics.trail.api.web;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an ID'ed consumer that has to receive at least one {@link WebMetric}.
 */
@XmlRootElement
@XmlType(propOrder = { "consumerId", "trails" })
public class WebMetricConsumer {

	@XmlElement(required = true, nillable = false)
	private String consumerId;
	@XmlElement(required = false, nillable = false)
	private List<WebMetricTrail> trails = new ArrayList<>();

	/**
	 * Default Constructor.
	 */
	public WebMetricConsumer() {
	}

	/**
	 * Constructor.
	 * 
	 * @param consumerId The id; might be null.
	 */
	public WebMetricConsumer(String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * Constructor.
	 * 
	 * @param consumerId The id; might be null.
	 * @param session    A session; might be null.
	 */
	public WebMetricConsumer(String consumerId, WebMetricTrail session) {
		this.consumerId = consumerId;
		if (session != null) {
			this.trails.add(session);
		}
	}

	/**
	 * Returns the consumer's ID.
	 * 
	 * @return The id, might be null
	 */
	public String getConsumerId() {
		return consumerId;
	}

	/**
	 * Sets the consumer's ID.
	 * 
	 * @param consumerId The id; might be null.
	 */
	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	/**
	 * Returns the trails in whose context this consumer has to receive at least 1
	 * metric each.
	 * 
	 * @return The session list, might be null
	 */
	public List<WebMetricTrail> getTrails() {
		return trails;
	}

	/**
	 * Sets the trails in whose context this consumer has to receive at least 1
	 * metric each.
	 * 
	 * @param trails The session list, might be null.
	 */
	public void setTrails(List<WebMetricTrail> trails) {
		this.trails = trails;
	}
}
