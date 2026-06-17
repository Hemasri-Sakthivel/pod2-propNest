package com.cog.propNest.module.maintenanceFacility.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkOrderDTO {

    private int workOrderId;
    private int requestId;
    private int technicianId;
    private LocalDate scheduledDate;
    private LocalDate actualVisitDate;
    private String workDescription;
    private double materialCost;
    private double labourCost;
    private String status;
}