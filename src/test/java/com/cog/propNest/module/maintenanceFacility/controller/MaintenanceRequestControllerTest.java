package com.cog.propNest.module.maintenanceFacility.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidRequestStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.MaintenanceRequestNotFoundException;
import com.cog.propNest.module.maintenanceFacility.service.MaintenanceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link MaintenanceRequestController} using standalone
 * MockMvc. The service is mocked and the real {@link GlobalExceptionHandler}
 * is registered, so the HTTP status mapping (200/201/404/409) is exercised.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceRequestControllerTest {

    private static final String BASE = "/propNest/maintenanceFacility";

    @Mock
    private MaintenanceRequestService maintenanceRequestService;

    @InjectMocks
    private MaintenanceRequestController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Map<String, Object> messageResponse(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("message", message);
        return m;
    }

    private Map<String, Object> dataResponse(Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("data", data);
        return m;
    }

    // ───────────────────────── createRequest ─────────────────────────

    @Test
    @DisplayName("POST /createRequest returns 201 CREATED")
    void createRequest_returns201() throws Exception {
        when(maintenanceRequestService.createRequest(any()))
                .thenReturn(messageResponse("Maintenance request created successfully"));

        mockMvc.perform(post(BASE + "/createRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitId\":1,\"category\":\"PL\",\"priority\":\"HI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Maintenance request created successfully"));
    }

    @Test
    @DisplayName("POST /createRequest delegates to the service")
    void createRequest_callsService() throws Exception {
        when(maintenanceRequestService.createRequest(any()))
                .thenReturn(messageResponse("ok"));

        mockMvc.perform(post(BASE + "/createRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitId\":1,\"category\":\"PL\",\"priority\":\"HI\"}"))
                .andExpect(status().isCreated());

        verify(maintenanceRequestService).createRequest(any(MaintenanceRequest.class));
    }

    // ───────────────────────── fetchAllRequests ─────────────────────────

    @Test
    @DisplayName("GET /fetchAllRequests returns 200 OK")
    void fetchAllRequests_returns200() throws Exception {
        when(maintenanceRequestService.fetchAllRequests())
                .thenReturn(dataResponse(List.of(new MaintenanceRequest())));

        mockMvc.perform(get(BASE + "/fetchAllRequests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ───────────────────────── fetchRequestById ─────────────────────────

    @Test
    @DisplayName("GET /fetchRequestById/{id} returns 200 when found")
    void fetchRequestById_returns200() throws Exception {
        when(maintenanceRequestService.fetchRequestById(1))
                .thenReturn(dataResponse(new MaintenanceRequest()));

        mockMvc.perform(get(BASE + "/fetchRequestById/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /fetchRequestById/{id} returns 404 when not found")
    void fetchRequestById_returns404() throws Exception {
        when(maintenanceRequestService.fetchRequestById(99))
                .thenThrow(new MaintenanceRequestNotFoundException(99));

        mockMvc.perform(get(BASE + "/fetchRequestById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ───────────────────────── fetchRequestsByUnit ─────────────────────────

    @Test
    @DisplayName("GET /fetchRequestsByUnit/{id} returns 200 OK")
    void fetchRequestsByUnit_returns200() throws Exception {
        when(maintenanceRequestService.fetchRequestsByUnit(1))
                .thenReturn(dataResponse(List.of(new MaintenanceRequest())));

        mockMvc.perform(get(BASE + "/fetchRequestsByUnit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ───────────────────────── fetchRequestsByTenant ─────────────────────────

    @Test
    @DisplayName("GET /fetchRequestsByTenant/{id} returns 200 OK")
    void fetchRequestsByTenant_returns200() throws Exception {
        when(maintenanceRequestService.fetchRequestsByTenant(3))
                .thenReturn(dataResponse(List.of(new MaintenanceRequest())));

        mockMvc.perform(get(BASE + "/fetchRequestsByTenant/3"))
                .andExpect(status().isOk());
    }

    // ───────────────────────── assignTechnician ─────────────────────────

    @Test
    @DisplayName("PUT /assignTechnician/{id}?techId= returns 200 OK")
    void assignTechnician_returns200() throws Exception {
        when(maintenanceRequestService.assignTechnician(1, 3))
                .thenReturn(messageResponse("Technician assigned successfully"));

        mockMvc.perform(put(BASE + "/assignTechnician/1").param("techId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Technician assigned successfully"));
    }

    @Test
    @DisplayName("PUT /assignTechnician/{id} returns 404 when not found")
    void assignTechnician_returns404() throws Exception {
        when(maintenanceRequestService.assignTechnician(eq(99), anyInt()))
                .thenThrow(new MaintenanceRequestNotFoundException(99));

        mockMvc.perform(put(BASE + "/assignTechnician/99").param("techId", "3"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── updateStatus ─────────────────────────

    @Test
    @DisplayName("PUT /updateStatus/{id}?status= returns 200 OK")
    void updateStatus_returns200() throws Exception {
        when(maintenanceRequestService.updateStatus(1, "AS"))
                .thenReturn(messageResponse("Status updated successfully"));

        mockMvc.perform(put(BASE + "/updateStatus/1").param("status", "AS"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /updateStatus/{id} returns 409 on invalid transition")
    void updateStatus_returns409() throws Exception {
        when(maintenanceRequestService.updateStatus(eq(1), eq("CL")))
                .thenThrow(new InvalidRequestStatusException("OP", "CL"));

        mockMvc.perform(put(BASE + "/updateStatus/1").param("status", "CL"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT /updateStatus/{id} returns 404 when not found")
    void updateStatus_returns404() throws Exception {
        when(maintenanceRequestService.updateStatus(eq(99), any()))
                .thenThrow(new MaintenanceRequestNotFoundException(99));

        mockMvc.perform(put(BASE + "/updateStatus/99").param("status", "AS"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── closeRequest ─────────────────────────

    @Test
    @DisplayName("PUT /closeRequest/{id} returns 200 OK")
    void closeRequest_returns200() throws Exception {
        when(maintenanceRequestService.closeRequest(1))
                .thenReturn(messageResponse("Request closed successfully"));

        mockMvc.perform(put(BASE + "/closeRequest/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /closeRequest/{id} returns 404 when not found")
    void closeRequest_returns404() throws Exception {
        when(maintenanceRequestService.closeRequest(99))
                .thenThrow(new MaintenanceRequestNotFoundException(99));

        mockMvc.perform(put(BASE + "/closeRequest/99"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── reopenRequest ─────────────────────────

    @Test
    @DisplayName("PUT /reopenRequest/{id} returns 200 OK")
    void reopenRequest_returns200() throws Exception {
        when(maintenanceRequestService.reopenRequest(1))
                .thenReturn(messageResponse("Request reopened successfully"));

        mockMvc.perform(put(BASE + "/reopenRequest/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /reopenRequest/{id} returns 404 when not found")
    void reopenRequest_returns404() throws Exception {
        when(maintenanceRequestService.reopenRequest(99))
                .thenThrow(new MaintenanceRequestNotFoundException(99));

        mockMvc.perform(put(BASE + "/reopenRequest/99"))
                .andExpect(status().isNotFound());
    }
}
