package com.cog.propNest.module.propertyListingPortfolio.service;

import com.cog.propNest.module.propertyListingPortfolio.dto.request.PropertyRequestDTO;
import com.cog.propNest.module.propertyListingPortfolio.dto.response.PropertyResponseDTO;
import com.cog.propNest.module.propertyListingPortfolio.entity.Property;
import com.cog.propNest.module.propertyListingPortfolio.entity.Unit;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicatePropertyException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidPropertyDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.propertyListingPortfolio.exception.PropertyNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.repository.PropertyRepository;
import com.cog.propNest.module.propertyListingPortfolio.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service-layer (business logic) tests for {@link PropertyService} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private PropertyService propertyService;

    private PropertyRequestDTO validRequest() {
        PropertyRequestDTO dto = new PropertyRequestDTO();
        dto.setOwnerId(1L);
        dto.setPropertyName("Sunrise Apartments");
        dto.setType("Residential");
        dto.setAddress("12 Anna Nagar");
        dto.setCity("Chennai");
        dto.setTotalUnits(5);
        dto.setYearBuilt(2015);
        return dto;
    }

    private Property property(Integer id, Property.PropertyStatus status) {
        Property p = new Property();
        p.setPropertyID(id);
        p.setOwnerID(1L);
        p.setPropertyName("Sunrise Apartments");
        p.setType(Property.PropertyType.Residential);
        p.setAddress("12 Anna Nagar");
        p.setCity("Chennai");
        p.setTotalUnits(5);
        p.setYearBuilt(2015);
        p.setStatus(status);
        return p;
    }

    // ── createProperty: success + validations ──
    @Test
    void createProperty_valid_savesActiveProperty() {
        when(propertyRepository.existsByOwnerIDAndPropertyNameAndCity(
                anyLong(), anyString(), anyString())).thenReturn(false);

        propertyService.createProperty(validRequest());

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(captor.capture());
        assertEquals(Property.PropertyType.Residential, captor.getValue().getType());
        assertEquals(Property.PropertyStatus.Active, captor.getValue().getStatus());
    }

    @Test
    void createProperty_nullOwnerId_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setOwnerId(null);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_blankPropertyName_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setPropertyName("   ");
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_blankAddress_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setAddress("");
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_blankCity_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setCity(null);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_invalidType_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setType("Bungalow");
        InvalidPropertyDataException ex = assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
        assertEquals("Invalid type. Must be Residential, Commercial or Mixed",
                ex.getMessage());
    }

    @Test
    void createProperty_zeroTotalUnits_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setTotalUnits(0);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_nullYearBuilt_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setYearBuilt(null);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_yearBuiltInFuture_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setYearBuilt(Year.now().getValue() + 5);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_yearBuiltBefore1900_throws() {
        PropertyRequestDTO dto = validRequest();
        dto.setYearBuilt(1800);
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.createProperty(dto));
    }

    @Test
    void createProperty_duplicate_throws() {
        when(propertyRepository.existsByOwnerIDAndPropertyNameAndCity(
                anyLong(), anyString(), anyString())).thenReturn(true);
        assertThrows(DuplicatePropertyException.class,
                () -> propertyService.createProperty(validRequest()));
        verify(propertyRepository, never()).save(any());
    }

    // ── reads ─────────────────────────────────
    @Test
    void getAllProperties_returnsActiveAsShortcodes() {
        when(propertyRepository.findByStatus(Property.PropertyStatus.Active))
                .thenReturn(List.of(property(1, Property.PropertyStatus.Active)));
        List<PropertyResponseDTO> result = propertyService.getAllProperties();
        assertEquals(1, result.size());
        assertEquals("AC", result.get(0).getStatus());
        assertEquals("RS", result.get(0).getType());
    }

    @Test
    void getPropertyById_delisted_returnsNull() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Delisted)));
        assertNull(propertyService.getPropertyById(1));
    }

    @Test
    void getPropertyById_notFound_returnsNull() {
        when(propertyRepository.findById(9)).thenReturn(Optional.empty());
        assertNull(propertyService.getPropertyById(9));
    }

    @Test
    void getPropertyById_active_returnsDto() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Active)));
        PropertyResponseDTO dto = propertyService.getPropertyById(1);
        assertEquals(1, dto.getPropertyId());
        assertEquals(1L, dto.getOwnerId());
        assertEquals("RS", dto.getType());
    }

    @Test
    void getPropertiesByOwner_returnsMapped() {
        when(propertyRepository.findByOwnerIDAndStatus(1L, Property.PropertyStatus.Active))
                .thenReturn(List.of(property(1, Property.PropertyStatus.Active)));
        assertEquals(1, propertyService.getPropertiesByOwner(1L).size());
    }

    @Test
    void getPropertiesByType_invalidType_throws() {
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.getPropertiesByType("Hut"));
    }

    @Test
    void getPropertiesByType_valid_returnsMapped() {
        when(propertyRepository.findByTypeAndStatus(
                Property.PropertyType.Residential, Property.PropertyStatus.Active))
                .thenReturn(List.of(property(1, Property.PropertyStatus.Active)));
        assertEquals(1, propertyService.getPropertiesByType("Residential").size());
    }

    @Test
    void getPropertiesByCity_blank_throws() {
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.getPropertiesByCity("  "));
    }

    @Test
    void getPropertiesByCity_valid_returnsMapped() {
        when(propertyRepository.findByCityIgnoreCaseAndStatus("Chennai",
                Property.PropertyStatus.Active))
                .thenReturn(List.of(property(1, Property.PropertyStatus.Active)));
        assertEquals(1, propertyService.getPropertiesByCity("Chennai").size());
    }

    // ── update / status / delist ──────────────
    @Test
    void updateProperty_notFound_throws() {
        when(propertyRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(PropertyNotFoundException.class,
                () -> propertyService.updateProperty(99, validRequest()));
    }

    @Test
    void updateProperty_delisted_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Delisted)));
        assertThrows(InvalidPropertyDataException.class,
                () -> propertyService.updateProperty(1, validRequest()));
    }

    @Test
    void updateProperty_success_updatesName() {
        Property existing = property(1, Property.PropertyStatus.Active);
        when(propertyRepository.findById(1)).thenReturn(Optional.of(existing));
        PropertyRequestDTO dto = new PropertyRequestDTO();
        dto.setPropertyName("Sunrise Heights");

        propertyService.updateProperty(1, dto);

        assertEquals("Sunrise Heights", existing.getPropertyName());
        verify(propertyRepository).save(existing);
    }

    @Test
    void updatePropertyStatus_validTransition_saves() {
        Property existing = property(1, Property.PropertyStatus.Active);
        when(propertyRepository.findById(1)).thenReturn(Optional.of(existing));

        propertyService.updatePropertyStatus(1, "UnderMaintenance");

        assertEquals(Property.PropertyStatus.UnderMaintenance, existing.getStatus());
        verify(propertyRepository).save(existing);
    }

    @Test
    void updatePropertyStatus_alreadyInStatus_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Active)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> propertyService.updatePropertyStatus(1, "Active"));
    }

    @Test
    void updatePropertyStatus_invalidTransition_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.UnderMaintenance)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> propertyService.updatePropertyStatus(1, "Delisted"));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void updatePropertyStatus_invalidValue_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Active)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> propertyService.updatePropertyStatus(1, "Banana"));
    }

    @Test
    void delistProperty_success_setsDelisted() {
        Property existing = property(1, Property.PropertyStatus.Active);
        when(propertyRepository.findById(1)).thenReturn(Optional.of(existing));
        when(unitRepository.existsByPropertyIDAndStatus(eq(1), eq(Unit.UnitStatus.Occupied)))
                .thenReturn(false);
        when(unitRepository.existsByPropertyIDAndStatus(eq(1), eq(Unit.UnitStatus.Reserved)))
                .thenReturn(false);

        propertyService.delistProperty(1);

        assertEquals(Property.PropertyStatus.Delisted, existing.getStatus());
        verify(propertyRepository).save(existing);
    }

    @Test
    void delistProperty_withOccupiedUnits_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Active)));
        when(unitRepository.existsByPropertyIDAndStatus(eq(1), eq(Unit.UnitStatus.Occupied)))
                .thenReturn(true);
        assertThrows(InvalidStatusTransitionException.class,
                () -> propertyService.delistProperty(1));
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void delistProperty_alreadyDelisted_throws() {
        when(propertyRepository.findById(1))
                .thenReturn(Optional.of(property(1, Property.PropertyStatus.Delisted)));
        assertThrows(InvalidStatusTransitionException.class,
                () -> propertyService.delistProperty(1));
    }

    @Test
    void delistProperty_notFound_throws() {
        when(propertyRepository.findById(7)).thenReturn(Optional.empty());
        assertThrows(PropertyNotFoundException.class,
                () -> propertyService.delistProperty(7));
    }
}
