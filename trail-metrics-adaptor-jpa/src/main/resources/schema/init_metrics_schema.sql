CREATE TABLE IF NOT EXISTS metric_trail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trail_id VARCHAR(36) NOT NULL,
    consumer_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS IDX_METRIC_TRAIL_TRAIL_ID ON metric_trail (trail_id);
CREATE INDEX IF NOT EXISTS IDX_METRIC_TRAIL_CONSUMER_ID ON metric_trail (consumer_id);
CREATE UNIQUE INDEX IF NOT EXISTS UIDX_METRIC_TRAIL_TRAIL_ID_CONSUMER_ID ON metric_trail (trail_id, consumer_id);

CREATE TABLE IF NOT EXISTS metric (
    id BIGINT NOT NULL AUTO_INCREMENT,
    metric_trail_id BIGINT NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    type ENUM('ALERT', 'PHASE', 'METER', 'TREND') NOT NULL,
    time_stamp DATETIME NOT NULL,
    time_zone VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_METRIC_TO_METRIC_TRAIL FOREIGN KEY (metric_trail_id) REFERENCES metric_trail (id)
);

CREATE INDEX IF NOT EXISTS IDX_METRIC_IDENTIFIER ON metric (identifier);
CREATE INDEX IF NOT EXISTS IDX_METRIC_TYPE ON metric (type);

CREATE TABLE IF NOT EXISTS metric_attribute (
    id BIGINT NOT NULL AUTO_INCREMENT,
    metric_id BIGINT NOT NULL,
    attribute_key VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_METRIC_ATTRIBUTE_TO_METRIC FOREIGN KEY (metric_id) REFERENCES metric (id)
);

CREATE INDEX IF NOT EXISTS IDX_ATTRIBUTE_KEY ON metric_attribute (attribute_key);
CREATE INDEX IF NOT EXISTS IDX_ATTRIBUTE_VALUE ON metric_attribute (attribute_value);
CREATE UNIQUE INDEX IF NOT EXISTS UIDX_METRIC_ATTRIBUTE_METRIC_ID_ATTRIBUTE_KEY ON metric_attribute (metric_id, attribute_key);