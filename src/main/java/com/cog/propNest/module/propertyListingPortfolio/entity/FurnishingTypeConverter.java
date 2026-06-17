package com.cog.propNest.module.propertyListingPortfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link Unit.FurnishingType} as its shortcode (UF | SF | FR) in the
 * {@code unit.furnishing} ENUM column.
 */
@Converter(autoApply = false)
public class FurnishingTypeConverter
        implements AttributeConverter<Unit.FurnishingType, String> {

    @Override
    public String convertToDatabaseColumn(Unit.FurnishingType furnishing) {
        return furnishing == null ? null : furnishing.getCode();
    }

    @Override
    public Unit.FurnishingType convertToEntityAttribute(String code) {
        return code == null ? null : Unit.FurnishingType.fromCode(code);
    }
}
