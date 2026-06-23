package com.cog.propNest.module
    .propertyListingPortfolio.dto.request;

import lombok.Data;

/**
 * Incoming property payload. Enum fields ({@code type}) are sent as full words
 * (e.g. "Residential"), not shortcodes.
 */
@Data
public class PropertyRequestDTO {
    private Long    ownerId;
    private String  propertyName;
    private String  type;
    private String  address;
    private String  city;
    private Integer totalUnits;
    private Integer yearBuilt;
    private String  status;
}
