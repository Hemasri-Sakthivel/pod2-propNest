package com.cog.propNest.module.propertyListingPortfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link Unit.UnitType} as its shortcode (ST | 1B | 2B | 3B | OF | SH)
 * in the {@code unit.type} ENUM column.
 */
@Converter(autoApply = false)
public class UnitTypeConverter
        implements AttributeConverter<Unit.UnitType, String> {

    @Override
    public String convertToDatabaseColumn(Unit.UnitType type) {
        return type == null ? null : type.getCode();
    }

    @Override
    public Unit.UnitType convertToEntityAttribute(String code) {
        return code == null ? null : Unit.UnitType.fromCode(code);
    }
}
