package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.web.WebMetricRequest;

/**
 * Interface for types that are able to deliver {@link WebMetricRequest}s per
 * web service.
 */
public interface MetricsWebFacade {

	/**
	 * Transfers the given request by web service.
	 * 
	 * @param request The request to transfer; might <b>not</b> be null.
	 * @throws Exception Any kind of array that might be caused by the transfer.
	 */
	void transfer(WebMetricRequest request) throws Exception;
}
