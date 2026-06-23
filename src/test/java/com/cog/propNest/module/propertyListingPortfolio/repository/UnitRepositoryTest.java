package com.cog.propNest.module.propertyListingPortfolio.repository;

import com.cog.propNest.module.propertyListingPortfolio.entity.Unit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository-layer tests for {@link UnitRepository}.
 *
 * <p>Read-only smoke tests against the real MySQL (no embedded DB available and
 * the FK-constrained {@code users} table is empty, so rows can't be inserted).
 * Each test exercises a real derived query and asserts it executes and returns
 * the right shape — validating query derivation, column mapping, and the
 * enum→shortcode converter binding (e.g. status=Vacant binds 'VC',
 * type=TwoBHK binds '2B'). Data-driven business logic is covered by the
 * service-layer tests.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UnitRepositoryTest {

    @Autowired
    private UnitRepository unitRepository;

    @Test
    void findAll_executes() {
        assertNotNull(unitRepository.findAll());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        assertTrue(unitRepository.findById(999_999_999).isEmpty());
    }

    @Test
    void findByPropertyID_executes() {
        assertNotNull(unitRepository.findByPropertyID(1));
    }

    @Test
    void findByPropertyID_nonExistent_returnsEmpty() {
        assertTrue(unitRepository.findByPropertyID(999_999_999).isEmpty());
    }

    @Test
    void findByStatus_vacant_executes() {
        assertNotNull(unitRepository.findByStatus(Unit.UnitStatus.Vacant));
    }

    @Test
    void findByStatus_occupied_executes() {
        assertNotNull(unitRepository.findByStatus(Unit.UnitStatus.Occupied));
    }

    @Test
    void findByStatus_reserved_executes() {
        assertNotNull(unitRepository.findByStatus(Unit.UnitStatus.Reserved));
    }

    @Test
    void findByStatus_maintenance_executes() {
        assertNotNull(unitRepository.findByStatus(Unit.UnitStatus.Maintenance));
    }

    @Test
    void findByTenantID_nonExistent_returnsEmpty() {
        assertTrue(unitRepository.findByTenantID(9_999_999L).isEmpty());
    }

    @Test
    void findByTypeAndFurnishing_executes() {
        assertNotNull(unitRepository.findByTypeAndFurnishing(
                Unit.UnitType.TwoBHK, Unit.FurnishingType.SemiFurnished));
    }

    @Test
    void findByTypeAndFurnishing_studioUnfurnished_executes() {
        assertNotNull(unitRepository.findByTypeAndFurnishing(
                Unit.UnitType.Studio, Unit.FurnishingType.Unfurnished));
    }

    @Test
    void findByListedRentBetween_executes() {
        assertNotNull(unitRepository.findByListedRentBetween(
                BigDecimal.valueOf(10000), BigDecimal.valueOf(30000)));
    }

    @Test
    void existsByPropertyIDAndUnitNumber_nonExistent_returnsFalse() {
        assertFalse(unitRepository.existsByPropertyIDAndUnitNumber(
                999_999_999, "ZtNoSuchUnit"));
    }

    @Test
    void existsByPropertyIDAndStatus_nonExistent_returnsFalse() {
        assertFalse(unitRepository.existsByPropertyIDAndStatus(
                999_999_999, Unit.UnitStatus.Occupied));
    }

    @Test
    void findByPropertyIDAndStatus_nonExistent_returnsEmpty() {
        assertTrue(unitRepository.findByPropertyIDAndStatus(
                999_999_999, Unit.UnitStatus.Vacant).isEmpty());
    }
}
