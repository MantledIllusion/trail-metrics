package com.mantledillusion.metrics.trail;

import javax.persistence.AttributeConverter;
import java.util.UUID;

/**
 * JPA {@link AttributeConverter} to convert a {@link UUID} to {@link String} and vice versa.
 */
public class UUIDStringAttributeConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return UUID.fromString(dbData);
    }
}
