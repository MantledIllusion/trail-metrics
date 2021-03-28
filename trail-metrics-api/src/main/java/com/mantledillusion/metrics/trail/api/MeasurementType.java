package com.mantledillusion.metrics.trail.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * The types of data an {@link Measurement} is allowed to carry.
 */
public enum MeasurementType {

    /**
     * A {@link String}.
     */
    STRING(val -> val),

    /**
     * A {@link Boolean}.
     */
    BOOLEAN(val -> {
        if (val.matches("true|false|TRUE|FALSE")) {
            return Boolean.parseBoolean(val);
        } else {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a Boolean", val));
        }
    }),

    /**
     * A {@link Short}.
     */
    SHORT(val -> {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a Short", val), e);
        }
    }),

    /**
     * An {@link Integer}.
     */
    INTEGER(val -> {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as an Integer", val), e);
        }
    }),

    /**
     * An {@link Long}.
     */
    LONG(val -> {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a Long", val), e);
        }
    }),

    /**
     * An {@link Float}.
     */
    FLOAT(val -> {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a Float", val), e);
        }
    }),

    /**
     * An {@link Double}.
     */
    DOUBLE(val -> {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a Double", val), e);
        }
    }),

    /**
     * A {@link BigInteger}
     */
    BIGINTEGER(val -> {
        try {
            return BigInteger.valueOf(Long.parseLong(val));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a BigInteger", val), e);
        }
    }),

    /**
     * A {@link BigDecimal}
     */
    BIGDECIMAL(val -> {
        try {
            return new DecimalFormat().parse(val);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a BigDecimal", val), e);
        }
    }),

    /**
     * A {@link LocalDate} in {@link DateTimeFormatter#ISO_LOCAL_DATE}.
     */
    LOCAL_DATE(val -> {
        try {
            return LocalDate.parse(val, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a LocalDate", val), e);
        }
    }),

    /**
     * A {@link LocalTime} in {@link DateTimeFormatter#ISO_LOCAL_TIME}.
     */
    LOCAL_TIME(val -> {
        try {
            return LocalTime.parse(val, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a LocalTime", val), e);
        }
    }),

    /**
     * A {@link LocalDateTime} in {@link DateTimeFormatter#ISO_DATE_TIME}.
     */
    LOCAL_DATETIME(val -> {
        try {
            return LocalDateTime.parse(val, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a LocalDateTime", val), e);
        }
    }),

    /**
     * A {@link ZonedDateTime} in {@link DateTimeFormatter#ISO_ZONED_DATE_TIME}.
     */
    ZONED_DATETIME(val -> {
        try {
            return ZonedDateTime.parse(val, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse measurement value %s as a ZonedDateTime", val), e);
        }
    });

    private final Function<String, ?> parser;

    MeasurementType(Function<String, ?> parser) {
        this.parser = parser;
    }

    /**
     * Parse the given measurement value as this element's type.
     *
     * @param <T> This elements type.
     * @param measurementValue The value to parse; might be null.
     * @return A value in this element's type, never null
     * @throws IllegalArgumentException If the given value is not parsable.
     */
    public <T> T parse(String measurementValue) {
        return (T) this.parser.apply(measurementValue == null ? "" : measurementValue);
    }

    /**
     * Returns if the given measurement value is a valid to be parsed to this element's type.
     *
     * @param measurementValue The value to check; might be null.
     * @return True if the value is valid, false otherwise
     */
    public boolean valid(String measurementValue) {
        try {
            parse(measurementValue);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
