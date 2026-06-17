package com.cog.propNest.module
    .propertyListingPortfolio.dto.response;

import lombok.Data;

/**
 * Outgoing unit payload. Enum fields ({@code type}, {@code furnishing},
 * {@code status}) are returned as shortcodes (e.g. "2B", "SF", "VC").
 */
@Data
public class UnitResponseDTO {
    private Integer unitId;
    private Integer propertyId;
    private Long    tenantId;
    private String  unitNumber;
    private Integer floor;
    private String  type;
    private Double  areaSqFt;
    private String  furnishing;
    private Double  listedRent;
    private String  status;
}
