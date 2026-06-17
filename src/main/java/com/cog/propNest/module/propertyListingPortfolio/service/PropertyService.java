package com.cog.propNest.module
    .propertyListingPortfolio.service;

import com.cog.propNest.module
    .propertyListingPortfolio.dto.request
    .PropertyRequestDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.dto.response
    .PropertyResponseDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.entity.Property;
import com.cog.propNest.module
    .propertyListingPortfolio.entity.Unit;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .DuplicatePropertyException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .InvalidPropertyDataException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .InvalidStatusTransitionException;
import com.cog.propNest.module
    .propertyListingPortfolio.exception
    .PropertyNotFoundException;
import com.cog.propNest.module
    .propertyListingPortfolio.repository
    .PropertyRepository;
import com.cog.propNest.module
    .propertyListingPortfolio.repository
    .UnitRepository;
import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;

    public PropertyService(PropertyRepository propertyRepository,
                           UnitRepository unitRepository) {
        this.propertyRepository = propertyRepository;
        this.unitRepository = unitRepository;
    }

    // ── toDTO (enums returned as shortcodes) ──
    private PropertyResponseDTO toDTO(Property p) {
        PropertyResponseDTO dto =
            new PropertyResponseDTO();
        dto.setPropertyId(p.getPropertyID());
        dto.setOwnerId(p.getOwnerID());
        dto.setPropertyName(p.getPropertyName());
        dto.setType(p.getType().getCode());
        dto.setAddress(p.getAddress());
        dto.setCity(p.getCity());
        dto.setTotalUnits(p.getTotalUnits());
        dto.setYearBuilt(p.getYearBuilt());
        dto.setStatus(p.getStatus().getCode());
        return dto;
    }

    // ── Validate and Create ───────────────────
    public void createProperty(
            PropertyRequestDTO dto) {

        if (dto.getOwnerId() == null)
            throw new InvalidPropertyDataException(
                "ownerId is required");

        if (dto.getPropertyName() == null ||
            dto.getPropertyName().isBlank())
            throw new InvalidPropertyDataException(
                "propertyName is required");

        if (dto.getAddress() == null ||
            dto.getAddress().isBlank())
            throw new InvalidPropertyDataException(
                "address is required");

        if (dto.getCity() == null ||
            dto.getCity().isBlank())
            throw new InvalidPropertyDataException(
                "city is required");

        if (dto.getType() == null ||
            dto.getType().isBlank())
            throw new InvalidPropertyDataException(
                "type is required");

        Property.PropertyType type;
        try {
            type = Property.PropertyType
                .fromWord(dto.getType());
        } catch (IllegalArgumentException e) {
            throw new InvalidPropertyDataException(
                "Invalid type. Must be " +
                "Residential, Commercial or Mixed");
        }

        if (dto.getTotalUnits() == null ||
            dto.getTotalUnits() <= 0)
            throw new InvalidPropertyDataException(
                "totalUnits must be greater than 0");

        if (dto.getYearBuilt() == null)
            throw new InvalidPropertyDataException(
                "yearBuilt is required");

        int currentYear = Year.now().getValue();
        if (dto.getYearBuilt() < 1900 ||
            dto.getYearBuilt() > currentYear)
            throw new InvalidPropertyDataException(
                "yearBuilt must be between " +
                "1900 and " + currentYear);

        if (propertyRepository
                .existsByOwnerIDAndPropertyNameAndCity(
                    dto.getOwnerId(),
                    dto.getPropertyName().trim(),
                    dto.getCity().trim()))
            throw new DuplicatePropertyException(
                "Property with same name already " +
                "exists in this city");

        Property p = new Property();
        p.setOwnerID(dto.getOwnerId());
        p.setPropertyName(
            dto.getPropertyName().trim());
        p.setType(type);
        p.setAddress(dto.getAddress().trim());
        p.setCity(dto.getCity().trim());
        p.setTotalUnits(dto.getTotalUnits());
        p.setYearBuilt(dto.getYearBuilt());
        p.setStatus(Property.PropertyStatus.Active);
        propertyRepository.save(p);
    }

    // ── GET All ───────────────────────────────
    public List<PropertyResponseDTO>
            getAllProperties() {
        return propertyRepository
            .findByStatus(
                Property.PropertyStatus.Active)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by ID ─────────────────────────────
    public PropertyResponseDTO getPropertyById(
            Integer propertyId) {
        Property p = propertyRepository
            .findById(propertyId).orElse(null);
        if (p == null || p.getStatus() ==
            Property.PropertyStatus.Delisted)
            return null;
        return toDTO(p);
    }

    // ── GET by Owner ──────────────────────────
    public List<PropertyResponseDTO>
            getPropertiesByOwner(Long ownerId) {
        return propertyRepository
            .findByOwnerIDAndStatus(
                ownerId,
                Property.PropertyStatus.Active)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by Type ───────────────────────────
    public List<PropertyResponseDTO>
            getPropertiesByType(String type) {
        Property.PropertyType t;
        try {
            t = Property.PropertyType.fromWord(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidPropertyDataException(
                "Invalid type. Must be " +
                "Residential, Commercial or Mixed");
        }
        return propertyRepository
            .findByTypeAndStatus(
                t,
                Property.PropertyStatus.Active)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── GET by City ───────────────────────────
    public List<PropertyResponseDTO>
            getPropertiesByCity(String city) {
        if (city == null || city.isBlank())
            throw new InvalidPropertyDataException(
                "city cannot be blank");
        return propertyRepository
            .findByCityIgnoreCaseAndStatus(
                city,
                Property.PropertyStatus.Active)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── UPDATE ────────────────────────────────
    public void updateProperty(
            Integer propertyId,
            PropertyRequestDTO dto) {

        Property existing = propertyRepository
            .findById(propertyId).orElse(null);

        if (existing == null)
            throw new PropertyNotFoundException(
                "Property not found");

        if (existing.getStatus() ==
            Property.PropertyStatus.Delisted)
            throw new InvalidPropertyDataException(
                "Cannot update a Delisted property");

        if (dto.getPropertyName() != null &&
            !dto.getPropertyName().isBlank())
            existing.setPropertyName(
                dto.getPropertyName().trim());

        if (dto.getAddress() != null &&
            !dto.getAddress().isBlank())
            existing.setAddress(
                dto.getAddress().trim());

        if (dto.getCity() != null &&
            !dto.getCity().isBlank())
            existing.setCity(dto.getCity().trim());

        if (dto.getTotalUnits() != null) {
            if (dto.getTotalUnits() <= 0)
                throw new InvalidPropertyDataException(
                    "totalUnits must be " +
                    "greater than 0");
            existing.setTotalUnits(
                dto.getTotalUnits());
        }

        propertyRepository.save(existing);
    }

    // ── UPDATE STATUS ─────────────────────────
    public void updatePropertyStatus(
            Integer propertyId, String newStatus) {

        Property existing = propertyRepository
            .findById(propertyId).orElse(null);

        if (existing == null)
            throw new PropertyNotFoundException(
                "Property not found");

        Property.PropertyStatus next;
        try {
            next = Property.PropertyStatus
                .fromWord(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException(
                "Invalid status value");
        }

        if (existing.getStatus() == next)
            throw new InvalidStatusTransitionException(
                "Property is already in " +
                existing.getStatus().getCode() +
                " status");

        boolean valid =
            (existing.getStatus() ==
             Property.PropertyStatus.Active &&
             next == Property.PropertyStatus
                 .UnderMaintenance) ||
            (existing.getStatus() ==
             Property.PropertyStatus.Active &&
             next == Property.PropertyStatus
                 .Delisted) ||
            (existing.getStatus() ==
             Property.PropertyStatus
                 .UnderMaintenance &&
             next == Property.PropertyStatus
                 .Active);

        if (!valid)
            throw new InvalidStatusTransitionException(
                "Status conflict, " +
                "transition not allowed");

        if (next ==
                Property.PropertyStatus.Delisted) {
            if (unitRepository
                    .existsByPropertyIDAndStatus(
                        propertyId,
                        Unit.UnitStatus.Occupied) ||
                unitRepository
                    .existsByPropertyIDAndStatus(
                        propertyId,
                        Unit.UnitStatus.Reserved))
                throw new InvalidStatusTransitionException(
                    "Cannot Delist property with " +
                    "Occupied or Reserved units");
        }

        existing.setStatus(next);
        propertyRepository.save(existing);
    }

    // ── DELIST ────────────────────────────────
    public void delistProperty(Integer propertyId) {

        Property existing = propertyRepository
            .findById(propertyId).orElse(null);

        if (existing == null)
            throw new PropertyNotFoundException(
                "Property not found");

        if (existing.getStatus() ==
            Property.PropertyStatus.Delisted)
            throw new InvalidStatusTransitionException(
                "Property is already Delisted");

        if (unitRepository
                .existsByPropertyIDAndStatus(
                    propertyId,
                    Unit.UnitStatus.Occupied) ||
            unitRepository
                .existsByPropertyIDAndStatus(
                    propertyId,
                    Unit.UnitStatus.Reserved))
            throw new InvalidStatusTransitionException(
                "Cannot Delist property with " +
                "Occupied or Reserved units");

        existing.setStatus(
            Property.PropertyStatus.Delisted);
        propertyRepository.save(existing);
    }
}
