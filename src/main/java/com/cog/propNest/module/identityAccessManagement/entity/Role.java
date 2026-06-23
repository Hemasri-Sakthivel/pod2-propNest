package com.cog.propNest.module.identityAccessManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One of the six PropNest roles. Seeded at startup; never created at runtime.
 * roleId values are fixed: 1=OWNER, 2=TENANT, 3=PROPERTY_MANAGER,
 * 4=TECHNICIAN, 5=FINANCE_EXECUTIVE, 6=REAL_ESTATE_ADMIN.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Column(name = "roleId")
    private Integer roleId;

    @Column(name = "roleName", nullable = false, length = 50)
    private String roleName;
}
