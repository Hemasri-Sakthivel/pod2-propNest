package com.cog.propNest.module
    .propertyListingPortfolio.dto.response;

import lombok.Data;

/**
 * Outgoing property payload. Enum fields ({@code type}, {@code status}) are
 * returned as shortcodes (e.g. "RS", "AC").
 */
@Data
public class PropertyResponseDTO {
    private Integer propertyId;
    private Long    ownerId;
    private String  propertyName;
    private String  type;
    private String  address;
    private String  city;
    private Integer totalUnits;
    private Integer yearBuilt;
    private String  status;
}
