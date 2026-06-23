package com.cog.propNest.module.propertyListingPortfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link Property.PropertyStatus} as its shortcode (AC | UM | DL) in the
 * {@code property.status} ENUM column.
 */
@Converter(autoApply = false)
public class PropertyStatusConverter
        implements AttributeConverter<Property.PropertyStatus, String> {

    @Override
    public String convertToDatabaseColumn(Property.PropertyStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public Property.PropertyStatus convertToEntityAttribute(String code) {
        return code == null ? null : Property.PropertyStatus.fromCode(code);
    }
}
