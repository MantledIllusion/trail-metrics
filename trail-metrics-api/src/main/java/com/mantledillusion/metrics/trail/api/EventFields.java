package com.mantledillusion.metrics.trail.api;

public enum EventFields {

    CONSUMER_ID("consumerId"),
    CORRELATION_ID("correlationId"),
    IDENTIFIER("identifier"),
    MEASUREMENTS("measurements"),
    MEASUREMENT_KEY("key"),
    MEASUREMENT_VALUE("value"),
    TIMESTAMP("timestamp");

    private final String name;

    EventFields(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getName(String prefix) {
        return prefix+name;
    }
}