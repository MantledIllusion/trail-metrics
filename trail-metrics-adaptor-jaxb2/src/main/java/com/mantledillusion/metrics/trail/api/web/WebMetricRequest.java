package com.mantledillusion.metrics.trail.api.web;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Request containing packaged {@link WebMetric}s.
 */
@XmlRootElement
@XmlType(propOrder = { "consumers" })
public class WebMetricRequest {

	@XmlElement(required = false, nillable = false)
	private List<WebMetricConsumer> consumers = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public WebMetricRequest() {
	}

	/**
	 * Constructor.
	 * 
	 * @param consumer
	 *            A consumer to add; might be null.
	 */
	public WebMetricRequest(WebMetricConsumer consumer) {
		if (consumer != null) {
			this.consumers.add(consumer);
		}
	}

	/**
	 * Returns a list of {@link WebMetricConsumer}s by their IDs that have to receive
	 * at least one {@link WebMetric}.
	 * 
	 * @return The consumer list, might be null
	 */
	public List<WebMetricConsumer> getConsumers() {
		return consumers;
	}

	/**
	 * Sets the list of {@link WebMetricConsumer}s by their IDs that have to receive
	 * at least one {@link WebMetric}.
	 * 
	 * @param consumers
	 *            The consumer list, might be null.
	 */
	public void setConsumers(List<WebMetricConsumer> consumers) {
		this.consumers = consumers;
	}
}
