package com.cog.propNest.module.maintenanceFacility.repository;

import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository-layer (persistence) tests for {@link WorkOrderRepository}.
 *
 * Runs against a throwaway MySQL schema ({@code propnest_test}) created on the
 * fly with {@code ddl-auto=create-drop}, so it never touches the real
 * {@code propnest} data. Each test runs in a transaction that is rolled back.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/propnest_test?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        "spring.datasource.username=root",
        "spring.datasource.password=root",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl"
})
class WorkOrderRepositoryTest {

    @Autowired
    private WorkOrderRepository repository;

    private WorkOrder newWorkOrder(int requestId, int techId, String status) {
        WorkOrder w = new WorkOrder();
        w.setRequestId(requestId);
        w.setTechnicianId(techId);
        w.setScheduledDate(LocalDate.now());
        w.setWorkDescription("Test work");
        w.setMaterialCost(0.0);
        w.setLabourCost(0.0);
        w.setStatus(status);
        return w;
    }

    @Test
    @DisplayName("save assigns a generated workOrderId")
    void save_assignsGeneratedId() {
        WorkOrder saved = repository.save(newWorkOrder(1, 4, "SC"));

        assertTrue(saved.getWorkOrderId() > 0);
    }

    @Test
    @DisplayName("findById returns the persisted work order")
    void findById_returnsSaved() {
        WorkOrder saved = repository.save(newWorkOrder(1, 4, "SC"));

        Optional<WorkOrder> found = repository.findById(saved.getWorkOrderId());

        assertTrue(found.isPresent());
        assertEquals(saved.getWorkOrderId(), found.get().getWorkOrderId());
    }

    @Test
    @DisplayName("save persists costs and status correctly")
    void save_persistsCostsAndStatus() {
        WorkOrder w = newWorkOrder(2, 7, "IP");
        w.setMaterialCost(750.0);
        w.setLabourCost(400.0);
        WorkOrder saved = repository.save(w);

        WorkOrder found = repository.findById(saved.getWorkOrderId()).orElseThrow();
        assertEquals(750.0, found.getMaterialCost());
        assertEquals(400.0, found.getLabourCost());
        assertEquals("IP", found.getStatus());
        assertEquals(2, found.getRequestId());
    }

    @Test
    @DisplayName("save allows a null actualVisitDate")
    void save_allowsNullActualVisitDate() {
        WorkOrder saved = repository.save(newWorkOrder(1, 4, "SC"));

        WorkOrder found = repository.findById(saved.getWorkOrderId()).orElseThrow();
        assertNull(found.getActualVisitDate());
    }

    @Test
    @DisplayName("findAll returns every persisted work order")
    void findAll_returnsAll() {
        repository.save(newWorkOrder(1, 4, "SC"));
        repository.save(newWorkOrder(2, 7, "IP"));

        List<WorkOrder> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findByTechnicianId returns only work orders for that technician")
    void findByTechnicianId_returnsMatching() {
        repository.save(newWorkOrder(1, 4, "SC"));
        repository.save(newWorkOrder(2, 4, "IP"));
        repository.save(newWorkOrder(3, 7, "SC"));

        List<WorkOrder> tech4 = repository.findByTechnicianId(4);

        assertEquals(2, tech4.size());
        assertTrue(tech4.stream().allMatch(w -> w.getTechnicianId() == 4));
    }

    @Test
    @DisplayName("findByTechnicianId returns empty list when none match")
    void findByTechnicianId_empty() {
        repository.save(newWorkOrder(1, 4, "SC"));

        assertTrue(repository.findByTechnicianId(99).isEmpty());
    }
}
