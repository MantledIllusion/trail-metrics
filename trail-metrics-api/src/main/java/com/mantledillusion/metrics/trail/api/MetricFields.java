package com.mantledillusion.metrics.trail.api;

public enum MetricFields {

    CONSUMER_ID("consumerId"),
    CORRELATION_ID("correlationId"),
    IDENTIFIER("identifier"),
    TYPE("type"),
    ATTRIBUTES("attributes"),
    ATTRIBUTE_KEY("key"),
    ATTRIBUTE_VALUE("value"),
    TIMESTAMP("timestamp");

    private final String name;

    MetricFields(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getName(String prefix) {
        return prefix+name;
    }
}