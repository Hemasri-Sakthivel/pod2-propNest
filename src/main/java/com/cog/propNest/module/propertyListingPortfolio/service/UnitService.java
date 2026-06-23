package com.cog.propNest.module
    .propertyListingPortfolio.service;

import com.cog.propNest.module
    .propertyListingPortfolio.dto.request
    .UnitRequestDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.dto.response
    .UnitResponseDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.entity.Property;
import com.cog.propNest.module
    .propertyListingPortfolio.entity.Unit;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .DuplicateUnitException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .InvalidStatusTransitionException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .InvalidUnitDataException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .PropertyNotFoundException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .UnitNotFoundException;
import com.cog.propNest.module
    .propertyListingPortfolio.repository
    .PropertyRepository;
import com.cog.propNest.module
    .propertyListingPortfolio.repository
    .UnitRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;

    public UnitService(UnitRepository unitRepository,
                       PropertyRepository propertyRepository) {
        this.unitRepository = unitRepository;
        this.propertyRepository = propertyRepository;
    }

    // ── toDTO (enums returned as shortcodes) ──
    private UnitResponseDTO toDTO(Unit u) {
        UnitResponseDTO dto = new UnitResponseDTO();
        dto.setUnitId(u.getUnitID());
        dto.setPropertyId(u.getPropertyID());
        dto.setTenantId(u.getTenantID());
        dto.setUnitNumber(u.getUnitNumber());
        dto.setFloor(u.getFloor());
        dto.setType(u.getType().getCode());
        dto.setAreaSqFt(u.getAreaSqFt() != null
            ? u.getAreaSqFt().doubleValue() : null);
        dto.setFurnishing(u.getFurnishing().getCode());
        dto.setListedRent(u.getListedRent() != null
            ? u.getListedRent().doubleValue() : null);
        dto.setStatus(u.getStatus().getCode());
        return dto;
    }

    // ── POST: Create Unit ─────────────────────
    public void createUnit(UnitRequestDTO dto) {

        if (dto.getPropertyId() == null)
            throw new InvalidUnitDataException(
                "propertyId is required");

        Property property = propertyRepository
            .findById(dto.getPropertyId())
            .orElse(null);

        if (property == null)
            throw new PropertyNotFoundException(
                "Property not found");

        if (property.getStatus() !=
            Property.PropertyStatus.Active)
            throw new InvalidUnitDataException(
                "Cannot add unit to a " +
                property.getStatus().getCode() +
                " property");

        if (dto.getUnitNumber() == null ||
            dto.getUnitNumber().isBlank())
            throw new InvalidUnitDataException(
                "unitNumber is required");

        if (dto.getFloor() == null ||
            dto.getFloor() < 0)
            throw new InvalidUnitDataException(
                "floor must be 0 or greater");

        if (dto.getType() == null ||
            dto.getType().isBlank())
            throw new InvalidUnitDataException(
                "type is required");

        Unit.UnitType type;
        try {
            type = Unit.UnitType.fromWord(dto.getType());
        } catch (IllegalArgumentException e) {
            throw new InvalidUnitDataException(
                "Invalid type. Must be Studio, " +
                "1BHK, 2BHK, 3BHK, Office or Shop");
        }

        if (dto.getAreaSqFt() == null ||
            dto.getAreaSqFt() <= 0)
            throw new InvalidUnitDataException(
                "areaSqFt must be greater than 0");

        if (dto.getListedRent() == null ||
            dto.getListedRent() <= 0)
            throw new InvalidUnitDataException(
                "listedRent must be greater than 0");

        if (dto.getFurnishing() == null ||
            dto.getFurnishing().isBlank())
            throw new InvalidUnitDataException(
                "furnishing is required");

        Unit.FurnishingType furnishing;
        try {
            furnishing = Unit.FurnishingType
                .fromWord(dto.getFurnishing());
        } catch (IllegalArgumentException e) {
            throw new InvalidUnitDataException(
                "Invalid furnishing. Must be " +
                "Unfurnished, SemiFurnished " +
                "or Furnished");
        }

        if (unitRepository
                .existsByPropertyIDAndUnitNumber(
                    dto.getPropertyId(),
                    dto.getUnitNumber().trim()))
            throw new DuplicateUnitException(
                "Unit number already exists " +
                "in this property");

        Unit u = new Unit();
        u.setPropertyID(dto.getPropertyId());
        u.setUnitNumber(dto.getUnitNumber().trim());
        u.setFloor(dto.getFloor());
        u.setType(type);
        u.setAreaSqFt(BigDecimal.valueOf(
            dto.getAreaSqFt()));
        u.setFurnishing(furnishing);
        u.setListedRent(BigDecimal.valueOf(
            dto.getListedRent()));
        u.setStatus(Unit.UnitStatus.Vacant);
        unitRepository.save(u);
    }

    // ── GET All ───────────────────────────────
    public List<UnitResponseDTO> getAllUnits() {
        return unitRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by ID ─────────────────────────────
    public UnitResponseDTO getUnitById(
            Integer unitId) {
        Unit u = unitRepository
            .findById(unitId).orElse(null);
        if (u == null) return null;
        return toDTO(u);
    }

    // ── GET by Property ───────────────────────
    public List<UnitResponseDTO> getUnitsByProperty(
            Integer propertyId) {
        return unitRepository
            .findByPropertyID(propertyId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET Vacant ────────────────────────────
    public List<UnitResponseDTO> getVacantUnits() {
        return unitRepository
            .findByStatus(Unit.UnitStatus.Vacant)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by Tenant ─────────────────────────
    public UnitResponseDTO getUnitByTenant(
            Long tenantId) {
        Unit u = unitRepository
            .findByTenantID(tenantId).orElse(null);
        if (u == null) return null;
        return toDTO(u);
    }

    // ── GET by Type and Furnishing ────────────
    public List<UnitResponseDTO>
            getUnitsByTypeAndFurnishing(
                String type, String furnishing) {
        Unit.UnitType unitType;
        try {
            unitType = Unit.UnitType.fromWord(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidUnitDataException(
                "Invalid type. Must be Studio, " +
                "1BHK, 2BHK, 3BHK, Office or Shop");
        }
        Unit.FurnishingType furnishingType;
        try {
            furnishingType = Unit.FurnishingType
                .fromWord(furnishing);
        } catch (IllegalArgumentException e) {
            throw new InvalidUnitDataException(
                "Invalid furnishing. Must be " +
                "Unfurnished, SemiFurnished " +
                "or Furnished");
        }
        return unitRepository
            .findByTypeAndFurnishing(
                unitType, furnishingType)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by Rent Range ─────────────────────
    public List<UnitResponseDTO> getUnitsByRentRange(
            Double minRent, Double maxRent) {
        if (minRent == null || maxRent == null)
            throw new InvalidUnitDataException(
                "minRent and maxRent are required");
        if (minRent <= 0 || maxRent <= 0)
            throw new InvalidUnitDataException(
                "minRent and maxRent must " +
                "be greater than 0");
        if (minRent >= maxRent)
            throw new InvalidUnitDataException(
                "minRent must be less than maxRent");
        return unitRepository
            .findByListedRentBetween(
                BigDecimal.valueOf(minRent),
                BigDecimal.valueOf(maxRent))
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── ASSIGN TENANT ─────────────────────────
    public void assignTenant(
            Integer unitId, Long tenantId) {

        Unit u = unitRepository
            .findById(unitId).orElse(null);

        if (u == null)
            throw new UnitNotFoundException(
                "Unit not found");

        if (u.getStatus() != Unit.UnitStatus.Vacant)
            throw new InvalidStatusTransitionException(
                "Unit is not vacant");

        if (tenantId == null)
            throw new InvalidUnitDataException(
                "tenantId is required");

        u.setTenantID(tenantId);
        u.setStatus(Unit.UnitStatus.Occupied);
        unitRepository.save(u);
    }

    // ── VACATE UNIT ───────────────────────────
    public void vacateUnit(Integer unitId) {

        Unit u = unitRepository
            .findById(unitId).orElse(null);

        if (u == null)
            throw new UnitNotFoundException(
                "Unit not found");

        if (u.getStatus() ==
            Unit.UnitStatus.Vacant)
            throw new InvalidStatusTransitionException(
                "Unit is already vacant");

        u.setTenantID(null);
        u.setStatus(Unit.UnitStatus.Vacant);
        unitRepository.save(u);
    }

    // ── UPDATE UNIT DETAILS ───────────────────
    public void updateUnitDetails(
            Integer unitId, UnitRequestDTO dto) {

        Unit existing = unitRepository
            .findById(unitId).orElse(null);

        if (existing == null)
            throw new UnitNotFoundException(
                "Unit not found");

        if (dto.getListedRent() != null) {
            if (dto.getListedRent() <= 0)
                throw new InvalidUnitDataException(
                    "listedRent must be " +
                    "greater than 0");
            existing.setListedRent(
                BigDecimal.valueOf(
                    dto.getListedRent()));
        }

        if (dto.getFurnishing() != null &&
            !dto.getFurnishing().isBlank()) {
            try {
                existing.setFurnishing(
                    Unit.FurnishingType.fromWord(
                        dto.getFurnishing()));
            } catch (IllegalArgumentException e) {
                throw new InvalidUnitDataException(
                    "Invalid furnishing. Must be " +
                    "Unfurnished, SemiFurnished " +
                    "or Furnished");
            }
        }

        unitRepository.save(existing);
    }

    // ── UPDATE UNIT STATUS ────────────────────
    public void updateUnitStatus(
            Integer unitId, String newStatus) {

        Unit existing = unitRepository
            .findById(unitId).orElse(null);

        if (existing == null)
            throw new UnitNotFoundException(
                "Unit not found");

        Unit.UnitStatus next;
        try {
            next = Unit.UnitStatus.fromWord(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException(
                "Invalid status value");
        }

        if (existing.getStatus() == next)
            throw new InvalidStatusTransitionException(
                "Unit is already in " +
                existing.getStatus().getCode() +
                " status");

        boolean valid =
            (existing.getStatus() ==
             Unit.UnitStatus.Vacant &&
             next == Unit.UnitStatus.Reserved) ||
            (existing.getStatus() ==
             Unit.UnitStatus.Reserved &&
             next == Unit.UnitStatus.Occupied) ||
            (existing.getStatus() ==
             Unit.UnitStatus.Occupied &&
             next == Unit.UnitStatus.Maintenance) ||
            (existing.getStatus() ==
             Unit.UnitStatus.Maintenance &&
             next == Unit.UnitStatus.Vacant);

        if (!valid)
            throw new InvalidStatusTransitionException(
                "Status conflict, " +
                "transition not allowed");

        existing.setStatus(next);
        unitRepository.save(existing);
    }

    // ── BULK UPDATE STATUS ────────────────────
    public void bulkUpdateStatus(
            Integer propertyId, String newStatus) {

        Property property = propertyRepository
            .findById(propertyId).orElse(null);

        if (property == null)
            throw new PropertyNotFoundException(
                "Property not found");

        List<Unit> units = unitRepository
            .findByPropertyID(propertyId);

        if (units.isEmpty())
            throw new UnitNotFoundException(
                "No units found for given property");

        Unit.UnitStatus next;
        try {
            next = Unit.UnitStatus.fromWord(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException(
                "Invalid status value");
        }

        units.forEach(u -> {
            if (u.getTenantID() == null)
                u.setStatus(next);
        });

        unitRepository.saveAll(units);
    }
}
