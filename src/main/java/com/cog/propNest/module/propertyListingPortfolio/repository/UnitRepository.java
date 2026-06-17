package com.cog.propNest.module
    .propertyListingPortfolio.repository;

import com.cog.propNest.module
    .propertyListingPortfolio.entity.Unit;
import org.springframework.data.jpa.repository
    .JpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UnitRepository
        extends JpaRepository<Unit, Integer> {

    List<Unit> findByPropertyID(
            Integer propertyID);

    List<Unit> findByStatus(
            Unit.UnitStatus status);

    Optional<Unit> findByTenantID(
            Long tenantID);

    List<Unit> findByTypeAndFurnishing(
            Unit.UnitType type,
            Unit.FurnishingType furnishing);

    List<Unit> findByListedRentBetween(
            BigDecimal minRent,
            BigDecimal maxRent);

    boolean existsByPropertyIDAndUnitNumber(
            Integer propertyID,
            String unitNumber);

    boolean existsByPropertyIDAndStatus(
            Integer propertyID,
            Unit.UnitStatus status);

    List<Unit> findByPropertyIDAndStatus(
            Integer propertyID,
            Unit.UnitStatus status);
}
