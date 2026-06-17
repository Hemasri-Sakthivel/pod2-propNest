package com.cog.propNest.module
    .propertyListingPortfolio.dto.request;

import lombok.Data;

/**
 * Incoming unit payload. Enum fields ({@code type}, {@code furnishing}) are sent
 * as full words (e.g. "2BHK", "SemiFurnished"), not shortcodes.
 */
@Data
public class UnitRequestDTO {
    private Integer propertyId;
    private String  unitNumber;
    private Integer floor;
    private String  type;
    private Double  areaSqFt;
    private String  furnishing;
    private Double  listedRent;
    private String  status;
}
