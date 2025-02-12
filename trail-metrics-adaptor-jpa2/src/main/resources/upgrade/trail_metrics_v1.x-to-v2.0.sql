/** Migrate Consumers **/
INSERT INTO trail_consumer (id, consumer_id, correlation_id)
    SELECT id, consumer_id, correlation_id
    FROM metric_trail;

/** Migrate Events **/
INSERT INTO trail_event (id, identifier, time_stamp, time_zone, trail_consumer_id)
    SELECT id, identifier, time_stamp, time_zone, metric_trail_id
    FROM metric;

/** Migrate Measurements **/
INSERT INTO trail_measurement (id, measurement_key, measurement_type, measurement_value, trail_event_id)
    SELECT id, attribute_key, 'STRING', attribute_value, metric_id
    FROM metric_attribute;