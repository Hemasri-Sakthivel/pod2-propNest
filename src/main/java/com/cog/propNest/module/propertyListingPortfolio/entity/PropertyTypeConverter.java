package com.cog.propNest.module.propertyListingPortfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link Property.PropertyType} as its shortcode (RS | CM | MX) in the
 * {@code property.type} ENUM column.
 */
@Converter(autoApply = false)
public class PropertyTypeConverter
        implements AttributeConverter<Property.PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(Property.PropertyType type) {
        return type == null ? null : type.getCode();
    }

    @Override
    public Property.PropertyType convertToEntityAttribute(String code) {
        return code == null ? null : Property.PropertyType.fromCode(code);
    }
}
