package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.Measurement;
import com.mantledillusion.metrics.trail.api.MeasurementType;

import javax.jms.JMSException;
import javax.jms.Message;

public class TrailMetricsMessageUtil {

    private static final String MID_RECEIVED = "spring.jms.message.receive";
    private static final String AKEY_DESTINATION = "destination";
    private static final String AKEY_ORIGINAL_CORRELATION_ID = "originalCorrelationId";

    static void writeReceiveMetric(Message message, String originalCorrelationId) {
        Event event = new Event(MID_RECEIVED);
        try {
            event.getMeasurements().add(new Measurement(AKEY_DESTINATION, message.getJMSDestination().toString(), MeasurementType.STRING));
            if (originalCorrelationId != null) {
                event.getMeasurements().add(new Measurement(AKEY_ORIGINAL_CORRELATION_ID, message.getJMSDestination().toString(), MeasurementType.STRING));
            }
        } catch (JMSException e) {
            // Ignore
        }
        MetricsTrailSupport.commit(event, false);
    }
}
