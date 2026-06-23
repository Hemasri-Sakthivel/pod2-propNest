package com.cog.propNest.module.rentDepositManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal response carrying only a status message, returned by the create and
 * update endpoints, e.g. {@code {"message": "Invoice created successfully"}}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;
}
