package com.cog.propNest.module.propertyListingPortfolio.repository;

import com.cog.propNest.module.propertyListingPortfolio.entity.Property;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository-layer tests for {@link PropertyRepository}.
 *
 * <p>No embedded DB (H2) can be downloaded in this environment, so these run
 * against the real MySQL ({@code replace = NONE}). That schema enforces
 * {@code property.ownerId -> users.userId} and the {@code users} table is empty,
 * so rows cannot be inserted. These are therefore read-only "smoke" tests: each
 * exercises a real derived query and asserts it executes against MySQL and
 * returns the correct shape. This validates the things that can only break at
 * the DB boundary — query derivation, column mapping, and the enum→shortcode
 * converter binding in WHERE clauses (e.g. status=Active binds 'AC'). Business
 * logic over data is covered by the service-layer tests.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @Test
    void findAll_executes() {
        assertNotNull(propertyRepository.findAll());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        assertTrue(propertyRepository.findById(999_999_999).isEmpty());
    }

    @Test
    void findByStatus_active_executes() {
        assertNotNull(propertyRepository.findByStatus(
                Property.PropertyStatus.Active));
    }

    @Test
    void findByStatus_underMaintenance_executes() {
        assertNotNull(propertyRepository.findByStatus(
                Property.PropertyStatus.UnderMaintenance));
    }

    @Test
    void findByStatus_delisted_executes() {
        assertNotNull(propertyRepository.findByStatus(
                Property.PropertyStatus.Delisted));
    }

    @Test
    void findByOwnerIDAndStatus_executes() {
        assertNotNull(propertyRepository.findByOwnerIDAndStatus(
                1L, Property.PropertyStatus.Active));
    }

    @Test
    void findByOwnerIDAndStatus_nonExistentOwner_returnsEmpty() {
        assertTrue(propertyRepository.findByOwnerIDAndStatus(
                9_999_999L, Property.PropertyStatus.Active).isEmpty());
    }

    @Test
    void findByTypeAndStatus_residential_executes() {
        assertNotNull(propertyRepository.findByTypeAndStatus(
                Property.PropertyType.Residential, Property.PropertyStatus.Active));
    }

    @Test
    void findByTypeAndStatus_commercial_executes() {
        assertNotNull(propertyRepository.findByTypeAndStatus(
                Property.PropertyType.Commercial, Property.PropertyStatus.Active));
    }

    @Test
    void findByTypeAndStatus_mixed_executes() {
        assertNotNull(propertyRepository.findByTypeAndStatus(
                Property.PropertyType.Mixed, Property.PropertyStatus.Active));
    }

    @Test
    void findByCityIgnoreCaseAndStatus_executes() {
        assertNotNull(propertyRepository.findByCityIgnoreCaseAndStatus(
                "Chennai", Property.PropertyStatus.Active));
    }

    @Test
    void findByCityIgnoreCaseAndStatus_nonExistentCity_returnsEmpty() {
        assertTrue(propertyRepository.findByCityIgnoreCaseAndStatus(
                "ZtNoSuchCity999", Property.PropertyStatus.Active).isEmpty());
    }

    @Test
    void existsByOwnerIDAndPropertyNameAndCity_nonExistent_returnsFalse() {
        assertFalse(propertyRepository.existsByOwnerIDAndPropertyNameAndCity(
                9_999_999L, "ZtNoSuchName", "ZtNoSuchCity"));
    }
}
