package com.cog.propNest.module.maintenanceFacility.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidWorkOrderStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.WorkOrderNotFoundException;
import com.cog.propNest.module.maintenanceFacility.service.WorkOrderService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link WorkOrderController} using standalone MockMvc.
 * The service is mocked and the real {@link GlobalExceptionHandler} is
 * registered, so the HTTP status mapping (200/201/404/409) is exercised.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderControllerTest {

    private static final String BASE = "/propNest/maintenanceFacility";

    @Mock
    private WorkOrderService workOrderService;

    @InjectMocks
    private WorkOrderController controller;

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

    // ───────────────────────── createWorkOrder ─────────────────────────

    @Test
    @DisplayName("POST /createWorkOrder returns 201 CREATED")
    void createWorkOrder_returns201() throws Exception {
        when(workOrderService.createWorkOrder(any()))
                .thenReturn(messageResponse("Work order created successfully"));

        mockMvc.perform(post(BASE + "/createWorkOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":1,\"technicianId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Work order created successfully"));
    }

    @Test
    @DisplayName("POST /createWorkOrder delegates to the service")
    void createWorkOrder_callsService() throws Exception {
        when(workOrderService.createWorkOrder(any())).thenReturn(messageResponse("ok"));

        mockMvc.perform(post(BASE + "/createWorkOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":1,\"technicianId\":3}"))
                .andExpect(status().isCreated());

        verify(workOrderService).createWorkOrder(any(WorkOrder.class));
    }

    // ───────────────────────── fetchAllWorkOrders ─────────────────────────

    @Test
    @DisplayName("GET /fetchAllWorkOrders returns 200 OK")
    void fetchAllWorkOrders_returns200() throws Exception {
        when(workOrderService.fetchAllWorkOrders())
                .thenReturn(dataResponse(List.of(new WorkOrder())));

        mockMvc.perform(get(BASE + "/fetchAllWorkOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ───────────────────────── fetchWorkOrderById ─────────────────────────

    @Test
    @DisplayName("GET /fetchWorkOrderById/{id} returns 200 when found")
    void fetchWorkOrderById_returns200() throws Exception {
        when(workOrderService.fetchWorkOrderById(1))
                .thenReturn(dataResponse(new WorkOrder()));

        mockMvc.perform(get(BASE + "/fetchWorkOrderById/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /fetchWorkOrderById/{id} returns 404 when not found")
    void fetchWorkOrderById_returns404() throws Exception {
        when(workOrderService.fetchWorkOrderById(99))
                .thenThrow(new WorkOrderNotFoundException(99));

        mockMvc.perform(get(BASE + "/fetchWorkOrderById/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ───────────────────────── fetchWorkOrdersByTechnician ─────────────────────────

    @Test
    @DisplayName("GET /fetchWorkOrdersByTechnician/{id} returns 200 OK")
    void fetchWorkOrdersByTechnician_returns200() throws Exception {
        when(workOrderService.fetchWorkOrdersByTechnician(3))
                .thenReturn(dataResponse(List.of(new WorkOrder())));

        mockMvc.perform(get(BASE + "/fetchWorkOrdersByTechnician/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ───────────────────────── updateWorkOrderStatus ─────────────────────────

    @Test
    @DisplayName("PUT /updateWorkOrderStatus/{id}?status= returns 200 OK")
    void updateWorkOrderStatus_returns200() throws Exception {
        when(workOrderService.updateWorkOrderStatus(1, "IP"))
                .thenReturn(messageResponse("Work order status updated successfully"));

        mockMvc.perform(put(BASE + "/updateWorkOrderStatus/1").param("status", "IP"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /updateWorkOrderStatus/{id} returns 409 on invalid transition")
    void updateWorkOrderStatus_returns409() throws Exception {
        when(workOrderService.updateWorkOrderStatus(eq(1), eq("CM")))
                .thenThrow(new InvalidWorkOrderStatusException("SC", "CM"));

        mockMvc.perform(put(BASE + "/updateWorkOrderStatus/1").param("status", "CM"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT /updateWorkOrderStatus/{id} returns 404 when not found")
    void updateWorkOrderStatus_returns404() throws Exception {
        when(workOrderService.updateWorkOrderStatus(eq(99), any()))
                .thenThrow(new WorkOrderNotFoundException(99));

        mockMvc.perform(put(BASE + "/updateWorkOrderStatus/99").param("status", "IP"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── updateWorkOrderCosts ─────────────────────────

    @Test
    @DisplayName("PUT /updateWorkOrderCosts/{id} returns 200 OK")
    void updateWorkOrderCosts_returns200() throws Exception {
        when(workOrderService.updateWorkOrderCosts(1, 750.0, 400.0))
                .thenReturn(messageResponse("Work order costs updated successfully"));

        mockMvc.perform(put(BASE + "/updateWorkOrderCosts/1")
                        .param("materialCost", "750.0")
                        .param("labourCost", "400.0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /updateWorkOrderCosts/{id} returns 404 when not found")
    void updateWorkOrderCosts_returns404() throws Exception {
        when(workOrderService.updateWorkOrderCosts(eq(99), anyDouble(), anyDouble()))
                .thenThrow(new WorkOrderNotFoundException(99));

        mockMvc.perform(put(BASE + "/updateWorkOrderCosts/99")
                        .param("materialCost", "750.0")
                        .param("labourCost", "400.0"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── completeWorkOrder ─────────────────────────

    @Test
    @DisplayName("PUT /completeWorkOrder/{id} returns 200 OK")
    void completeWorkOrder_returns200() throws Exception {
        when(workOrderService.completeWorkOrder(1))
                .thenReturn(messageResponse("Work order completed successfully"));

        mockMvc.perform(put(BASE + "/completeWorkOrder/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /completeWorkOrder/{id} returns 404 when not found")
    void completeWorkOrder_returns404() throws Exception {
        when(workOrderService.completeWorkOrder(99))
                .thenThrow(new WorkOrderNotFoundException(99));

        mockMvc.perform(put(BASE + "/completeWorkOrder/99"))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── cancelWorkOrder ─────────────────────────

    @Test
    @DisplayName("PUT /cancelWorkOrder/{id} returns 200 OK")
    void cancelWorkOrder_returns200() throws Exception {
        when(workOrderService.cancelWorkOrder(1))
                .thenReturn(messageResponse("Work order cancelled successfully"));

        mockMvc.perform(put(BASE + "/cancelWorkOrder/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Work order cancelled successfully"));
    }

    @Test
    @DisplayName("PUT /cancelWorkOrder/{id} returns 409 when not Scheduled")
    void cancelWorkOrder_returns409() throws Exception {
        when(workOrderService.cancelWorkOrder(1))
                .thenThrow(new InvalidWorkOrderStatusException("Work order cannot be cancelled - already IP"));

        mockMvc.perform(put(BASE + "/cancelWorkOrder/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT /cancelWorkOrder/{id} returns 404 when not found")
    void cancelWorkOrder_returns404() throws Exception {
        when(workOrderService.cancelWorkOrder(99))
                .thenThrow(new WorkOrderNotFoundException(99));

        mockMvc.perform(put(BASE + "/cancelWorkOrder/99"))
                .andExpect(status().isNotFound());
    }
}
