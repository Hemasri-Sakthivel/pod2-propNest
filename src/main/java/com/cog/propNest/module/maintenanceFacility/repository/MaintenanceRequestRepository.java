package com.cog.propNest.module.maintenanceFacility.repository;

import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository
        extends JpaRepository<MaintenanceRequest, Integer> {

    List<MaintenanceRequest> findByUnitId(int unitId);

    List<MaintenanceRequest> findByAssignedTechId(int assignedTechId);
}