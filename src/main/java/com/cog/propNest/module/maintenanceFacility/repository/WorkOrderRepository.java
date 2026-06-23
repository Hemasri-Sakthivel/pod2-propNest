package com.cog.propNest.module.maintenanceFacility.repository;

import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkOrderRepository
        extends JpaRepository<WorkOrder, Integer> {

    List<WorkOrder> findByTechnicianId(int technicianId);
}