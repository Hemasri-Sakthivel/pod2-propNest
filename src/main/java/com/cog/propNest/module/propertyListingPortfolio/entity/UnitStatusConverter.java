package com.cog.propNest.module.propertyListingPortfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link Unit.UnitStatus} as its shortcode (VC | OC | RS | MN) in the
 * {@code unit.status} ENUM column.
 */
@Converter(autoApply = false)
public class UnitStatusConverter
        implements AttributeConverter<Unit.UnitStatus, String> {

    @Override
    public String convertToDatabaseColumn(Unit.UnitStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public Unit.UnitStatus convertToEntityAttribute(String code) {
        return code == null ? null : Unit.UnitStatus.fromCode(code);
    }
}
