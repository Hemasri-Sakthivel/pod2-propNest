package com.cog.propNest.module.maintenanceFacility.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "work_order")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workOrderId")
    private int workOrderId;

    @Column(name = "requestId")
    private int requestId;

    @Column(name = "technicianId")
    private int technicianId;

    @Column(name = "scheduledDate")
    private LocalDate scheduledDate;

    @Column(name = "actualVisitDate")
    private LocalDate actualVisitDate;

    @Column(name = "workDescription")
    private String workDescription;

    @Column(name = "materialCost")
    private double materialCost;

    @Column(name = "labourCost")
    private double labourCost;

    // ENUM('SC','IP','CM','CN')
    @Column(name = "status")
    private String status;
}
