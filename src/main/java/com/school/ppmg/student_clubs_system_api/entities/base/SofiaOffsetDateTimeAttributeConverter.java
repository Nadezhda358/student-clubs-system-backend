package com.school.ppmg.student_clubs_system_api.entities.base;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Converter(autoApply = true)
public class SofiaOffsetDateTimeAttributeConverter implements AttributeConverter<OffsetDateTime, LocalDateTime> {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Sofia");

    @Override
    public LocalDateTime convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(LocalDateTime dbData) {
        if (dbData == null) {
            return null;
        }

        return dbData.atZone(BUSINESS_ZONE).toOffsetDateTime();
    }
}
