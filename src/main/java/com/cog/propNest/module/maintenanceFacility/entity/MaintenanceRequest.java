package com.cog.propNest.module.maintenanceFacility.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestId")
    private int requestId;

    @Column(name = "unitId")
    private int unitId;

    @Column(name = "assignedTechId")
    private Integer assignedTechId;

    // ENUM('PL','EL','HV','AP','ST','PS')
    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    // ENUM('LO','MD','HI','EM')
    @Column(name = "priority")
    private String priority;

    @Column(name = "raisedDate")
    private LocalDate raisedDate;

    @Column(name = "resolvedDate")
    private LocalDate resolvedDate;

    // ENUM('OP','AS','IP','RS','CL','RO')
    @Column(name = "status")
    private String status;
}
