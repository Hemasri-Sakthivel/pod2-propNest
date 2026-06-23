package com.cog.propNest.module.maintenanceFacility.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MaintenanceRequestDTO {

    private int requestId;
    private int unitId;
    private Integer assignedTechId;
    private String category;
    private String description;
    private String priority;
    private LocalDate raisedDate;
    private LocalDate resolvedDate;
    private String status;
}