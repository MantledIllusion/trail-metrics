CREATE TABLE IF NOT EXISTS trail_consumer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    correlation_id VARCHAR(36) NOT NULL,
    consumer_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS IDX_TRAIL_CORRELATION_ID ON trail_consumer (correlation_id);
CREATE INDEX IF NOT EXISTS IDX_TRAIL_CONSUMER_ID ON trail_consumer (consumer_id);
CREATE UNIQUE INDEX IF NOT EXISTS UIDX_TRAIL_CORRELATION_ID_CONSUMER_ID ON trail_consumer (correlation_id, consumer_id);

CREATE TABLE IF NOT EXISTS trail_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trail_consumer_id BIGINT NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    time_stamp DATETIME NOT NULL,
    time_zone VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_EVENT_TO_TRAIL FOREIGN KEY (trail_consumer_id) REFERENCES trail_consumer (id)
);

CREATE INDEX IF NOT EXISTS IDX_METRIC_IDENTIFIER ON trail_event (identifier);

CREATE TABLE IF NOT EXISTS trail_measurement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trail_event_id BIGINT NOT NULL,
    measurement_key VARCHAR(255) NOT NULL,
    measurement_value VARCHAR(2047) NULL,
    measurement_type ENUM('STRING', 'BOOLEAN', 'SHORT', 'INTEGER', 'LONG', 'FLOAT', 'DOUBLE', 'BIGINTEGER', 'BIGDECIMAL', 'LOCAL_DATE', 'LOCAL_TIME', 'LOCAL_DATETIME', 'ZONED_DATETIME') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_MEASUREMENT_TO_EVENT FOREIGN KEY (trail_event_id) REFERENCES trail_event (id)
);

CREATE INDEX IF NOT EXISTS IDX_ATTRIBUTE_KEY ON trail_measurement (measurement_key);
CREATE INDEX IF NOT EXISTS IDX_ATTRIBUTE_VALUE ON trail_measurement (measurement_value);
CREATE INDEX IF NOT EXISTS IDX_ATTRIBUTE_TYPE ON trail_measurement (measurement_type);
CREATE UNIQUE INDEX IF NOT EXISTS UIDX_MEASUREMENT_EVENT_ID_MEASUREMENT_KEY ON trail_measurement (trail_event_id, measurement_key);