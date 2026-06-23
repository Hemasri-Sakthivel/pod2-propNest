package com.cog.propNest.module.propertyListingPortfolio.controller;

import com.cog.propNest.module.propertyListingPortfolio.dto.response.PropertyResponseDTO;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicatePropertyException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidPropertyDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.propertyListingPortfolio.exception.PropertyNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests for {@link PropertyController} using @WebMvcTest +
 * MockMvc. Verifies the spec's per-endpoint HTTP status codes and JSON bodies.
 */
@WebMvcTest(PropertyController.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    private PropertyResponseDTO sampleDto() {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setPropertyId(1);
        dto.setOwnerId(1L);
        dto.setPropertyName("Sunrise Apartments");
        dto.setType("RS");
        dto.setCity("Chennai");
        dto.setStatus("AC");
        return dto;
    }

    private static final String VALID_BODY = """
        {"ownerId":1,"propertyName":"Sunrise Apartments","type":"Residential",
         "address":"12 Anna Nagar","city":"Chennai","totalUnits":5,"yearBuilt":2015}
        """;

    // ── createProperty ────────────────────────
    @Test
    void createProperty_valid_returns201() throws Exception {
        doNothing().when(propertyService).createProperty(any());
        mockMvc.perform(post("/propNest/propertyListing/createProperty")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Property created successfully"));
    }

    @Test
    void createProperty_invalidData_returns400() throws Exception {
        doThrow(new InvalidPropertyDataException("propertyName is required"))
                .when(propertyService).createProperty(any());
        mockMvc.perform(post("/propNest/propertyListing/createProperty")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("propertyName is required"));
    }

    @Test
    void createProperty_duplicate_returns400() throws Exception {
        doThrow(new DuplicatePropertyException(
                "Property with same name already exists in this city"))
                .when(propertyService).createProperty(any());
        mockMvc.perform(post("/propNest/propertyListing/createProperty")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest());
    }

    // ── fetchAllProperties ────────────────────
    @Test
    void fetchAll_nonEmpty_returns200() throws Exception {
        when(propertyService.getAllProperties()).thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchAllProperties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(1))
                .andExpect(jsonPath("$[0].status").value("AC"));
    }

    @Test
    void fetchAll_empty_returns404() throws Exception {
        when(propertyService.getAllProperties()).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchAllProperties"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No properties found"));
    }

    // ── fetchPropertyById ─────────────────────
    @Test
    void fetchById_found_returns200() throws Exception {
        when(propertyService.getPropertyById(1)).thenReturn(sampleDto());
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertyById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("RS"));
    }

    @Test
    void fetchById_notFound_returns404() throws Exception {
        when(propertyService.getPropertyById(99)).thenReturn(null);
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertyById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Property not found"));
    }

    // ── fetchByOwner ──────────────────────────
    @Test
    void fetchByOwner_nonEmpty_returns200() throws Exception {
        when(propertyService.getPropertiesByOwner(anyLong()))
                .thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByOwner/1"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByOwner_empty_returns404() throws Exception {
        when(propertyService.getPropertiesByOwner(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByOwner/1"))
                .andExpect(status().isNotFound());
    }

    // ── fetchByType ───────────────────────────
    @Test
    void fetchByType_valid_returns200() throws Exception {
        when(propertyService.getPropertiesByType("Residential"))
                .thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByType/Residential"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByType_empty_returns404() throws Exception {
        when(propertyService.getPropertiesByType("Mixed")).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByType/Mixed"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchByType_invalid_returns400() throws Exception {
        when(propertyService.getPropertiesByType("Hut"))
                .thenThrow(new InvalidPropertyDataException(
                        "Invalid type. Must be Residential, Commercial or Mixed"));
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByType/Hut"))
                .andExpect(status().isBadRequest());
    }

    // ── fetchByCity ───────────────────────────
    @Test
    void fetchByCity_valid_returns200() throws Exception {
        when(propertyService.getPropertiesByCity("Chennai"))
                .thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByCity/Chennai"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByCity_empty_returns404() throws Exception {
        when(propertyService.getPropertiesByCity("Delhi")).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchPropertiesByCity/Delhi"))
                .andExpect(status().isNotFound());
    }

    // ── updateProperty ────────────────────────
    @Test
    void updateProperty_valid_returns200() throws Exception {
        doNothing().when(propertyService).updateProperty(eq(1), any());
        mockMvc.perform(put("/propNest/propertyListing/updateProperty/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyName\":\"Sunrise Heights\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Property updated successfully"));
    }

    @Test
    void updateProperty_notFound_returns400() throws Exception {
        doThrow(new PropertyNotFoundException("Property not found"))
                .when(propertyService).updateProperty(eq(99), any());
        mockMvc.perform(put("/propNest/propertyListing/updateProperty/99")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Property not found"));
    }

    // ── updatePropertyStatus ──────────────────
    @Test
    void updateStatus_valid_returns200() throws Exception {
        doNothing().when(propertyService).updatePropertyStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/updatePropertyStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UnderMaintenance\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_missingStatus_returns400() throws Exception {
        mockMvc.perform(put("/propNest/propertyListing/updatePropertyStatus/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status is required"));
    }

    @Test
    void updateStatus_conflict_returns409() throws Exception {
        doThrow(new InvalidStatusTransitionException(
                "Status conflict, transition not allowed"))
                .when(propertyService).updatePropertyStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/updatePropertyStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Delisted\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Status conflict, transition not allowed"));
    }

    // ── delistProperty ────────────────────────
    @Test
    void delist_valid_returns200() throws Exception {
        doNothing().when(propertyService).delistProperty(1);
        mockMvc.perform(put("/propNest/propertyListing/delistProperty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Property delisted successfully"));
    }

    @Test
    void delist_blocked_returns400() throws Exception {
        doThrow(new InvalidStatusTransitionException(
                "Cannot Delist property with Occupied or Reserved units"))
                .when(propertyService).delistProperty(1);
        mockMvc.perform(put("/propNest/propertyListing/delistProperty/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delist_notFound_returns400() throws Exception {
        doThrow(new PropertyNotFoundException("Property not found"))
                .when(propertyService).delistProperty(7);
        mockMvc.perform(put("/propNest/propertyListing/delistProperty/7"))
                .andExpect(status().isBadRequest());
    }
}
