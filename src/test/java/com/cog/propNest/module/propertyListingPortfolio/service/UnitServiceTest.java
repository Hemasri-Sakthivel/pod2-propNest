package com.cog.propNest.module.propertyListingPortfolio.service;

import com.cog.propNest.module.propertyListingPortfolio.dto.request.UnitRequestDTO;
import com.cog.propNest.module.propertyListingPortfolio.dto.response.UnitResponseDTO;
import com.cog.propNest.module.propertyListingPortfolio.entity.Property;
import com.cog.propNest.module.propertyListingPortfolio.entity.Unit;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicateUnitException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidUnitDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.PropertyNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.exception.UnitNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.repository.PropertyRepository;
import com.cog.propNest.module.propertyListingPortfolio.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service-layer (business logic) tests for {@link UnitService} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private UnitService unitService;

    private UnitRequestDTO validRequest() {
        UnitRequestDTO dto = new UnitRequestDTO();
        dto.setPropertyId(1);
        dto.setUnitNumber("A-101");
        dto.setFloor(1);
        dto.setType("2BHK");
        dto.setAreaSqFt(950.0);
        dto.setFurnishing("Furnished");
        dto.setListedRent(25000.0);
        return dto;
    }

    private Property property(Property.PropertyStatus status) {
        Property p = new Property();
        p.setPropertyID(1);
        p.setStatus(status);
        return p;
    }

    private Unit unit(Integer id, Unit.UnitStatus status) {
        Unit u = new Unit();
        u.setUnitID(id);
        u.setPropertyID(1);
        u.setUnitNumber("A-101");
        u.setFloor(1);
        u.setType(Unit.UnitType.TwoBHK);
        u.setAreaSqFt(BigDecimal.valueOf(950.0));
        u.setFurnishing(Unit.FurnishingType.Furnished);
        u.setListedRent(BigDecimal.valueOf(25000.0));
        u.setStatus(status);
        return u;
    }

    // ── createUnit ────────────────────────────
    @Test
    void createUnit_valid_savesVacantUnit() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        when(unitRepository.existsByPropertyIDAndUnitNumber(anyInt(), anyString()))
                .thenReturn(false);

        unitService.createUnit(validRequest());

        ArgumentCaptor<Unit> captor = ArgumentCaptor.forClass(Unit.class);
        verify(unitRepository).save(captor.capture());
        assertEquals(Unit.UnitStatus.Vacant, captor.getValue().getStatus());
        assertEquals(Unit.UnitType.TwoBHK, captor.getValue().getType());
    }

    @Test
    void createUnit_nullPropertyId_throws() {
        UnitRequestDTO dto = validRequest();
        dto.setPropertyId(null);
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_propertyNotFound_throws() {
        when(propertyRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(PropertyNotFoundException.class,
                () -> unitService.createUnit(validRequest()));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void createUnit_propertyNotActive_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Delisted)));
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(validRequest()));
    }

    @Test
    void createUnit_blankUnitNumber_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setUnitNumber("  ");
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_negativeFloor_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setFloor(-1);
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_invalidType_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setType("5BHK");
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_zeroAreaSqFt_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setAreaSqFt(0.0);
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_zeroListedRent_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setListedRent(0.0);
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_invalidFurnishing_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        UnitRequestDTO dto = validRequest();
        dto.setFurnishing("Luxury");
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.createUnit(dto));
    }

    @Test
    void createUnit_duplicateUnitNumber_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        when(unitRepository.existsByPropertyIDAndUnitNumber(anyInt(), anyString()))
                .thenReturn(true);
        assertThrows(DuplicateUnitException.class,
                () -> unitService.createUnit(validRequest()));
        verify(unitRepository, never()).save(any());
    }

    // ── reads ─────────────────────────────────
    @Test
    void getAllUnits_returnsShortcodes() {
        when(unitRepository.findAll())
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        List<UnitResponseDTO> result = unitService.getAllUnits();
        assertEquals(1, result.size());
        assertEquals("2B", result.get(0).getType());
        assertEquals("FR", result.get(0).getFurnishing());
        assertEquals("VC", result.get(0).getStatus());
    }

    @Test
    void getUnitById_notFound_returnsNull() {
        when(unitRepository.findById(50)).thenReturn(Optional.empty());
        assertNull(unitService.getUnitById(50));
    }

    @Test
    void getUnitById_found_returnsDto() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        assertEquals(1, unitService.getUnitById(1).getUnitId());
    }

    @Test
    void getUnitsByProperty_returnsMapped() {
        when(unitRepository.findByPropertyID(1))
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        assertEquals(1, unitService.getUnitsByProperty(1).size());
    }

    @Test
    void getVacantUnits_returnsMapped() {
        when(unitRepository.findByStatus(Unit.UnitStatus.Vacant))
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        assertEquals(1, unitService.getVacantUnits().size());
    }

    @Test
    void getUnitByTenant_found_returnsDto() {
        Unit occupied = unit(1, Unit.UnitStatus.Occupied);
        occupied.setTenantID(7L);
        when(unitRepository.findByTenantID(7L)).thenReturn(Optional.of(occupied));
        assertEquals(7L, unitService.getUnitByTenant(7L).getTenantId());
    }

    @Test
    void getUnitByTenant_notFound_returnsNull() {
        when(unitRepository.findByTenantID(8L)).thenReturn(Optional.empty());
        assertNull(unitService.getUnitByTenant(8L));
    }

    @Test
    void getUnitsByTypeAndFurnishing_valid_returnsMapped() {
        when(unitRepository.findByTypeAndFurnishing(
                Unit.UnitType.TwoBHK, Unit.FurnishingType.Furnished))
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        assertEquals(1, unitService.getUnitsByTypeAndFurnishing(
                "2BHK", "Furnished").size());
    }

    @Test
    void getUnitsByTypeAndFurnishing_invalidType_throws() {
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.getUnitsByTypeAndFurnishing("9BHK", "Furnished"));
    }

    @Test
    void getUnitsByTypeAndFurnishing_invalidFurnishing_throws() {
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.getUnitsByTypeAndFurnishing("2BHK", "Royal"));
    }

    @Test
    void getUnitsByRentRange_nullParams_throws() {
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.getUnitsByRentRange(null, 30000.0));
    }

    @Test
    void getUnitsByRentRange_minGreaterThanMax_throws() {
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.getUnitsByRentRange(30000.0, 15000.0));
    }

    @Test
    void getUnitsByRentRange_valid_returnsMapped() {
        when(unitRepository.findByListedRentBetween(any(), any()))
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        assertEquals(1, unitService.getUnitsByRentRange(15000.0, 30000.0).size());
    }

    // ── tenant / status / details ─────────────
    @Test
    void assignTenant_notFound_throws() {
        when(unitRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UnitNotFoundException.class,
                () -> unitService.assignTenant(1, 7L));
    }

    @Test
    void assignTenant_notVacant_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Occupied)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> unitService.assignTenant(1, 7L));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void assignTenant_nullTenantId_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.assignTenant(1, null));
    }

    @Test
    void assignTenant_success_marksOccupied() {
        Unit u = unit(1, Unit.UnitStatus.Vacant);
        when(unitRepository.findById(1)).thenReturn(Optional.of(u));

        unitService.assignTenant(1, 7L);

        assertEquals(Unit.UnitStatus.Occupied, u.getStatus());
        assertEquals(7L, u.getTenantID());
        verify(unitRepository).save(u);
    }

    @Test
    void vacateUnit_notFound_throws() {
        when(unitRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UnitNotFoundException.class,
                () -> unitService.vacateUnit(1));
    }

    @Test
    void vacateUnit_alreadyVacant_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> unitService.vacateUnit(1));
    }

    @Test
    void vacateUnit_success_clearsTenant() {
        Unit u = unit(1, Unit.UnitStatus.Occupied);
        u.setTenantID(7L);
        when(unitRepository.findById(1)).thenReturn(Optional.of(u));

        unitService.vacateUnit(1);

        assertEquals(Unit.UnitStatus.Vacant, u.getStatus());
        assertNull(u.getTenantID());
        verify(unitRepository).save(u);
    }

    @Test
    void updateUnitDetails_notFound_throws() {
        when(unitRepository.findById(1)).thenReturn(Optional.empty());
        UnitRequestDTO dto = new UnitRequestDTO();
        dto.setListedRent(27000.0);
        assertThrows(UnitNotFoundException.class,
                () -> unitService.updateUnitDetails(1, dto));
    }

    @Test
    void updateUnitDetails_invalidFurnishing_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        UnitRequestDTO dto = new UnitRequestDTO();
        dto.setFurnishing("Royal");
        assertThrows(InvalidUnitDataException.class,
                () -> unitService.updateUnitDetails(1, dto));
    }

    @Test
    void updateUnitDetails_success_updatesRent() {
        Unit u = unit(1, Unit.UnitStatus.Vacant);
        when(unitRepository.findById(1)).thenReturn(Optional.of(u));
        UnitRequestDTO dto = new UnitRequestDTO();
        dto.setListedRent(27000.0);

        unitService.updateUnitDetails(1, dto);

        assertEquals(0, u.getListedRent().compareTo(BigDecimal.valueOf(27000.0)));
        verify(unitRepository).save(u);
    }

    @Test
    void updateUnitStatus_notFound_throws() {
        when(unitRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UnitNotFoundException.class,
                () -> unitService.updateUnitStatus(1, "Reserved"));
    }

    @Test
    void updateUnitStatus_invalidTransition_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> unitService.updateUnitStatus(1, "Occupied"));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void updateUnitStatus_alreadyInStatus_throws() {
        when(unitRepository.findById(1))
                .thenReturn(Optional.of(unit(1, Unit.UnitStatus.Vacant)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> unitService.updateUnitStatus(1, "Vacant"));
    }

    @Test
    void updateUnitStatus_validTransition_saves() {
        Unit u = unit(1, Unit.UnitStatus.Vacant);
        when(unitRepository.findById(1)).thenReturn(Optional.of(u));

        unitService.updateUnitStatus(1, "Reserved");

        assertEquals(Unit.UnitStatus.Reserved, u.getStatus());
        verify(unitRepository).save(u);
    }

    @Test
    void bulkUpdateStatus_propertyNotFound_throws() {
        when(propertyRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(PropertyNotFoundException.class,
                () -> unitService.bulkUpdateStatus(1, "Maintenance"));
    }

    @Test
    void bulkUpdateStatus_noUnits_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        when(unitRepository.findByPropertyID(1)).thenReturn(Collections.emptyList());
        assertThrows(UnitNotFoundException.class,
                () -> unitService.bulkUpdateStatus(1, "Reserved"));
    }

    @Test
    void bulkUpdateStatus_invalidStatus_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        when(unitRepository.findByPropertyID(1))
                .thenReturn(List.of(unit(1, Unit.UnitStatus.Vacant)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> unitService.bulkUpdateStatus(1, "Nonsense"));
    }

    @Test
    void bulkUpdateStatus_success_skipsOccupied() {
        Unit vacant = unit(1, Unit.UnitStatus.Vacant);
        Unit occupied = unit(2, Unit.UnitStatus.Occupied);
        occupied.setTenantID(5L);
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(Property.PropertyStatus.Active)));
        when(unitRepository.findByPropertyID(1))
                .thenReturn(List.of(vacant, occupied));

        unitService.bulkUpdateStatus(1, "Reserved");

        assertEquals(Unit.UnitStatus.Reserved, vacant.getStatus());
        assertEquals(Unit.UnitStatus.Occupied, occupied.getStatus());
        verify(unitRepository).saveAll(any());
    }
}
