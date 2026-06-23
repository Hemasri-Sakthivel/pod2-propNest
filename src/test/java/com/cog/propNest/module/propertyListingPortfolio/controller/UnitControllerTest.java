package com.cog.propNest.module.propertyListingPortfolio.controller;

import com.cog.propNest.module.propertyListingPortfolio.dto.response.UnitResponseDTO;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicateUnitException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidUnitDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.UnitNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.service.UnitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
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
 * Controller-layer tests for {@link UnitController} using @WebMvcTest + MockMvc.
 * Verifies the spec's per-endpoint HTTP status codes and JSON bodies.
 */
@WebMvcTest(UnitController.class)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnitService unitService;

    private UnitResponseDTO sampleDto() {
        UnitResponseDTO dto = new UnitResponseDTO();
        dto.setUnitId(1);
        dto.setPropertyId(1);
        dto.setUnitNumber("A-101");
        dto.setFloor(1);
        dto.setType("2B");
        dto.setFurnishing("SF");
        dto.setListedRent(25000.0);
        dto.setStatus("VC");
        return dto;
    }

    private static final String VALID_BODY = """
        {"propertyId":1,"unitNumber":"A-101","floor":1,"type":"2BHK",
         "areaSqFt":950.00,"furnishing":"SemiFurnished","listedRent":25000.00}
        """;

    // ── createUnit ────────────────────────────
    @Test
    void createUnit_valid_returns201() throws Exception {
        doNothing().when(unitService).createUnit(any());
        mockMvc.perform(post("/propNest/propertyListing/createUnit")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Unit created successfully"));
    }

    @Test
    void createUnit_invalid_returns400() throws Exception {
        doThrow(new InvalidUnitDataException("listedRent must be greater than 0"))
                .when(unitService).createUnit(any());
        mockMvc.perform(post("/propNest/propertyListing/createUnit")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("listedRent must be greater than 0"));
    }

    @Test
    void createUnit_duplicate_returns400() throws Exception {
        doThrow(new DuplicateUnitException("Unit number already exists in this property"))
                .when(unitService).createUnit(any());
        mockMvc.perform(post("/propNest/propertyListing/createUnit")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest());
    }

    // ── fetchAllUnits ─────────────────────────
    @Test
    void fetchAll_nonEmpty_returns200() throws Exception {
        when(unitService.getAllUnits()).thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchAllUnits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitId").value(1))
                .andExpect(jsonPath("$[0].type").value("2B"));
    }

    @Test
    void fetchAll_empty_returns404() throws Exception {
        when(unitService.getAllUnits()).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchAllUnits"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No units found"));
    }

    // ── fetchUnitById ─────────────────────────
    @Test
    void fetchById_found_returns200() throws Exception {
        when(unitService.getUnitById(1)).thenReturn(sampleDto());
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VC"));
    }

    @Test
    void fetchById_notFound_returns404() throws Exception {
        when(unitService.getUnitById(99)).thenReturn(null);
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unit not found"));
    }

    // ── fetchByProperty ───────────────────────
    @Test
    void fetchByProperty_nonEmpty_returns200() throws Exception {
        when(unitService.getUnitsByProperty(1)).thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByProperty/1"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByProperty_empty_returns404() throws Exception {
        when(unitService.getUnitsByProperty(1)).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByProperty/1"))
                .andExpect(status().isNotFound());
    }

    // ── fetchVacantUnits ──────────────────────
    @Test
    void fetchVacant_nonEmpty_returns200() throws Exception {
        when(unitService.getVacantUnits()).thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchVacantUnits"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchVacant_empty_returns404() throws Exception {
        when(unitService.getVacantUnits()).thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchVacantUnits"))
                .andExpect(status().isNotFound());
    }

    // ── fetchByTenant ─────────────────────────
    @Test
    void fetchByTenant_found_returns200() throws Exception {
        when(unitService.getUnitByTenant(anyLong())).thenReturn(sampleDto());
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitByTenant/2"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByTenant_notFound_returns404() throws Exception {
        when(unitService.getUnitByTenant(anyLong())).thenReturn(null);
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitByTenant/2"))
                .andExpect(status().isNotFound());
    }

    // ── fetchByTypeAndFurnishing ──────────────
    @Test
    void fetchByTypeAndFurnishing_valid_returns200() throws Exception {
        when(unitService.getUnitsByTypeAndFurnishing(anyString(), anyString()))
                .thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByTypeAndFurnishing")
                        .param("type", "2BHK").param("furnishing", "SemiFurnished"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByTypeAndFurnishing_empty_returns404() throws Exception {
        when(unitService.getUnitsByTypeAndFurnishing(anyString(), anyString()))
                .thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByTypeAndFurnishing")
                        .param("type", "2BHK").param("furnishing", "Furnished"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchByTypeAndFurnishing_invalid_returns400() throws Exception {
        when(unitService.getUnitsByTypeAndFurnishing(anyString(), anyString()))
                .thenThrow(new InvalidUnitDataException("Invalid type. Must be Studio, "
                        + "1BHK, 2BHK, 3BHK, Office or Shop"));
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByTypeAndFurnishing")
                        .param("type", "9BHK").param("furnishing", "Furnished"))
                .andExpect(status().isBadRequest());
    }

    // ── fetchByRentRange ──────────────────────
    @Test
    void fetchByRentRange_valid_returns200() throws Exception {
        when(unitService.getUnitsByRentRange(anyDouble(), anyDouble()))
                .thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByRentRange")
                        .param("minRent", "15000").param("maxRent", "30000"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchByRentRange_empty_returns404() throws Exception {
        when(unitService.getUnitsByRentRange(anyDouble(), anyDouble()))
                .thenReturn(List.of());
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByRentRange")
                        .param("minRent", "100").param("maxRent", "200"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchByRentRange_invalid_returns400() throws Exception {
        when(unitService.getUnitsByRentRange(anyDouble(), anyDouble()))
                .thenThrow(new InvalidUnitDataException("minRent must be less than maxRent"));
        mockMvc.perform(get("/propNest/propertyListing/fetchUnitsByRentRange")
                        .param("minRent", "30000").param("maxRent", "15000"))
                .andExpect(status().isBadRequest());
    }

    // ── assignTenant ──────────────────────────
    @Test
    void assignTenant_valid_returns200() throws Exception {
        doNothing().when(unitService).assignTenant(eq(1), anyLong());
        mockMvc.perform(put("/propNest/propertyListing/assignTenant/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Tenant assigned successfully, unit marked Occupied"));
    }

    @Test
    void assignTenant_notVacant_returns400() throws Exception {
        doThrow(new InvalidStatusTransitionException("Unit is not vacant"))
                .when(unitService).assignTenant(eq(1), anyLong());
        mockMvc.perform(put("/propNest/propertyListing/assignTenant/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unit is not vacant"));
    }

    @Test
    void assignTenant_unitNotFound_returns400() throws Exception {
        doThrow(new UnitNotFoundException("Unit not found"))
                .when(unitService).assignTenant(eq(9), anyLong());
        mockMvc.perform(put("/propNest/propertyListing/assignTenant/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":2}"))
                .andExpect(status().isBadRequest());
    }

    // ── vacateUnit ────────────────────────────
    @Test
    void vacateUnit_valid_returns200() throws Exception {
        doNothing().when(unitService).vacateUnit(1);
        mockMvc.perform(put("/propNest/propertyListing/vacateUnit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Unit vacated successfully, status set to Vacant"));
    }

    @Test
    void vacateUnit_alreadyVacant_returns400() throws Exception {
        doThrow(new InvalidStatusTransitionException("Unit is already vacant"))
                .when(unitService).vacateUnit(1);
        mockMvc.perform(put("/propNest/propertyListing/vacateUnit/1"))
                .andExpect(status().isBadRequest());
    }

    // ── updateUnitDetails ─────────────────────
    @Test
    void updateUnitDetails_valid_returns200() throws Exception {
        doNothing().when(unitService).updateUnitDetails(eq(1), any());
        mockMvc.perform(put("/propNest/propertyListing/updateUnitDetails/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"listedRent\":27000.00,\"furnishing\":\"Furnished\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unit updated successfully"));
    }

    @Test
    void updateUnitDetails_notFound_returns400() throws Exception {
        doThrow(new UnitNotFoundException("Unit not found"))
                .when(unitService).updateUnitDetails(eq(9), any());
        mockMvc.perform(put("/propNest/propertyListing/updateUnitDetails/9")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── updateUnitStatus ──────────────────────
    @Test
    void updateUnitStatus_valid_returns200() throws Exception {
        doNothing().when(unitService).updateUnitStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/updateUnitStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Reserved\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateUnitStatus_missingStatus_returns400() throws Exception {
        mockMvc.perform(put("/propNest/propertyListing/updateUnitStatus/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status is required"));
    }

    @Test
    void updateUnitStatus_conflict_returns409() throws Exception {
        doThrow(new InvalidStatusTransitionException(
                "Status conflict, transition not allowed"))
                .when(unitService).updateUnitStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/updateUnitStatus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Occupied\"}"))
                .andExpect(status().isConflict());
    }

    // ── bulkUpdate ────────────────────────────
    @Test
    void bulkUpdate_valid_returns200() throws Exception {
        doNothing().when(unitService).bulkUpdateStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/bulkUpdateUnitStatusByProperty/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Maintenance\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All units updated successfully"));
    }

    @Test
    void bulkUpdate_missingStatus_returns400() throws Exception {
        mockMvc.perform(put("/propNest/propertyListing/bulkUpdateUnitStatusByProperty/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkUpdate_propertyNotFound_returns400() throws Exception {
        doThrow(new com.cog.propNest.module.propertyListingPortfolio.exception
                .PropertyNotFoundException("Property not found"))
                .when(unitService).bulkUpdateStatus(eq(9), anyString());
        mockMvc.perform(put("/propNest/propertyListing/bulkUpdateUnitStatusByProperty/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Maintenance\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkUpdate_noUnits_returns400() throws Exception {
        doThrow(new UnitNotFoundException("No units found for given property"))
                .when(unitService).bulkUpdateStatus(eq(1), anyString());
        mockMvc.perform(put("/propNest/propertyListing/bulkUpdateUnitStatusByProperty/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Maintenance\"}"))
                .andExpect(status().isBadRequest());
    }
}
