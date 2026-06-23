package com.cog.propNest.module.maintenanceFacility.repository;

import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
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
 * Repository-layer (persistence) tests for {@link MaintenanceRequestRepository}.
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
class MaintenanceRequestRepositoryTest {

    @Autowired
    private MaintenanceRequestRepository repository;

    private MaintenanceRequest newRequest(int unitId, Integer techId, String category, String status) {
        MaintenanceRequest r = new MaintenanceRequest();
        r.setUnitId(unitId);
        r.setAssignedTechId(techId);
        r.setCategory(category);
        r.setDescription("Test description");
        r.setPriority("HI");
        r.setRaisedDate(LocalDate.now());
        r.setStatus(status);
        return r;
    }

    @Test
    @DisplayName("save assigns a generated requestId")
    void save_assignsGeneratedId() {
        MaintenanceRequest saved = repository.save(newRequest(1, 4, "PL", "OP"));

        assertTrue(saved.getRequestId() > 0);
    }

    @Test
    @DisplayName("findById returns the persisted request")
    void findById_returnsSaved() {
        MaintenanceRequest saved = repository.save(newRequest(1, 4, "PL", "OP"));

        Optional<MaintenanceRequest> found = repository.findById(saved.getRequestId());

        assertTrue(found.isPresent());
        assertEquals(saved.getRequestId(), found.get().getRequestId());
    }

    @Test
    @DisplayName("save persists all column values correctly")
    void save_persistsAllFields() {
        MaintenanceRequest saved = repository.save(newRequest(2, 7, "EL", "AS"));

        MaintenanceRequest found = repository.findById(saved.getRequestId()).orElseThrow();
        assertEquals(2, found.getUnitId());
        assertEquals(7, found.getAssignedTechId());
        assertEquals("EL", found.getCategory());
        assertEquals("HI", found.getPriority());
        assertEquals("AS", found.getStatus());
        assertEquals(LocalDate.now(), found.getRaisedDate());
    }

    @Test
    @DisplayName("save allows a null assignedTechId (unassigned request)")
    void save_allowsNullAssignedTechId() {
        MaintenanceRequest saved = repository.save(newRequest(1, null, "HV", "OP"));

        MaintenanceRequest found = repository.findById(saved.getRequestId()).orElseThrow();
        assertNull(found.getAssignedTechId());
    }

    @Test
    @DisplayName("findAll returns every persisted request")
    void findAll_returnsAll() {
        repository.save(newRequest(1, 4, "PL", "OP"));
        repository.save(newRequest(2, 7, "EL", "IP"));

        List<MaintenanceRequest> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findByUnitId returns only requests for that unit")
    void findByUnitId_returnsMatching() {
        repository.save(newRequest(1, 4, "PL", "OP"));
        repository.save(newRequest(1, 7, "EL", "IP"));
        repository.save(newRequest(2, 4, "HV", "OP"));

        List<MaintenanceRequest> unit1 = repository.findByUnitId(1);

        assertEquals(2, unit1.size());
        assertTrue(unit1.stream().allMatch(r -> r.getUnitId() == 1));
    }

    @Test
    @DisplayName("findByUnitId returns empty list when no requests for the unit")
    void findByUnitId_empty() {
        repository.save(newRequest(1, 4, "PL", "OP"));

        assertTrue(repository.findByUnitId(99).isEmpty());
    }

    @Test
    @DisplayName("findByAssignedTechId returns only requests for that technician")
    void findByAssignedTechId_returnsMatching() {
        repository.save(newRequest(1, 4, "PL", "OP"));
        repository.save(newRequest(2, 4, "EL", "IP"));
        repository.save(newRequest(3, 7, "HV", "OP"));

        List<MaintenanceRequest> tech4 = repository.findByAssignedTechId(4);

        assertEquals(2, tech4.size());
        assertTrue(tech4.stream().allMatch(r -> r.getAssignedTechId() == 4));
    }

    @Test
    @DisplayName("findByAssignedTechId returns empty list when none match")
    void findByAssignedTechId_empty() {
        repository.save(newRequest(1, 4, "PL", "OP"));

        assertTrue(repository.findByAssignedTechId(99).isEmpty());
    }
}
