package com.mantledillusion.metrics.trail;

import jakarta.persistence.AttributeConverter;
import java.time.ZoneId;

/**
 * JPA {@link AttributeConverter} to convert a {@link ZoneId} to {@link String} and vice versa.
 */
public class ZoneIdStringAttributeConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId attribute) {
        return attribute.getId();
    }

    @Override
    public ZoneId convertToEntityAttribute(String dbData) {
        return ZoneId.of(dbData);
    }
}
