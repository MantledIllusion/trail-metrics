package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.MetricAttribute;
import com.mantledillusion.metrics.trail.api.MetricType;

import javax.jms.JMSException;
import javax.jms.Message;

public class TrailMetricsMessageUtil {

    static final String MID_RECEIVED = "spring.jms.message.receive";
    static final String AKEY_DESTINATION = "destination";
    static final String AKEY_ORIGINAL_CORRELATION_ID = "originalCorrelationId";

    static void writeReceiveMetric(Message message, String originalDestinationId) {
        Metric metric = new Metric(MID_RECEIVED, MetricType.ALERT);
        try {
            metric.getAttributes().add(new MetricAttribute(AKEY_DESTINATION, message.getJMSDestination().toString()));
            if (originalDestinationId != null) {
                metric.getAttributes().add(new MetricAttribute(AKEY_ORIGINAL_CORRELATION_ID, message.getJMSDestination().toString()));
            }
        } catch (JMSException e) {
            // Ignore
        }
        MetricsTrailSupport.commit(metric, false);
    }
}
