package com.cog.propNest.module.maintenanceFacility.service;

import com.cog.propNest.module.maintenanceFacility.entity.WorkOrder;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidWorkOrderStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.WorkOrderNotFoundException;
import com.cog.propNest.module.maintenanceFacility.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WorkOrderService} using JUnit 5 + Mockito.
 * The repository is mocked; no Spring context or database is required.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderService workOrderService;

    private WorkOrder sampleWorkOrder;

    @BeforeEach
    void setUp() {
        sampleWorkOrder = new WorkOrder();
        sampleWorkOrder.setWorkOrderId(1);
        sampleWorkOrder.setRequestId(1);
        sampleWorkOrder.setTechnicianId(3);
        sampleWorkOrder.setScheduledDate(LocalDate.now());
        sampleWorkOrder.setWorkDescription("Fix water leakage pipe in bathroom");
        sampleWorkOrder.setMaterialCost(0.0);
        sampleWorkOrder.setLabourCost(0.0);
        sampleWorkOrder.setStatus("SC");
    }

    // ───────────────────────── createWorkOrder ─────────────────────────

    @Test
    @DisplayName("createWorkOrder sets status SC, zeroes costs, and saves")
    void createWorkOrder_setsDefaultsAndSaves() {
        WorkOrder incoming = new WorkOrder();
        incoming.setRequestId(1);
        incoming.setTechnicianId(3);
        incoming.setMaterialCost(999.0);
        incoming.setLabourCost(999.0);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> response = workOrderService.createWorkOrder(incoming);

        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(captor.capture());
        WorkOrder saved = captor.getValue();
        assertEquals("SC", saved.getStatus());
        assertEquals(0.0, saved.getMaterialCost());
        assertEquals(0.0, saved.getLabourCost());
        assertEquals("Work order created successfully", response.get("message"));
        // createWorkOrder returns a confirmation message only (no data payload)
        assertNull(response.get("data"));
    }

    // ───────────────────────── fetchAllWorkOrders ─────────────────────────

    @Test
    @DisplayName("fetchAllWorkOrders returns the list")
    void fetchAllWorkOrders_returnsList() {
        List<WorkOrder> list = List.of(sampleWorkOrder);
        when(workOrderRepository.findAll()).thenReturn(list);

        Map<String, Object> response = workOrderService.fetchAllWorkOrders();

        assertEquals(list, response.get("data"));
    }

    @Test
    @DisplayName("fetchAllWorkOrders returns an empty list when none exist")
    void fetchAllWorkOrders_returnsEmptyList() {
        when(workOrderRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> response = workOrderService.fetchAllWorkOrders();

        assertTrue(((List<?>) response.get("data")).isEmpty());
    }

    // ───────────────────────── fetchWorkOrderById ─────────────────────────

    @Test
    @DisplayName("fetchWorkOrderById returns the work order when found")
    void fetchWorkOrderById_found() {
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.fetchWorkOrderById(1);

        assertEquals(sampleWorkOrder, response.get("data"));
    }

    @Test
    @DisplayName("fetchWorkOrderById throws WorkOrderNotFoundException when missing")
    void fetchWorkOrderById_notFound_throws() {
        when(workOrderRepository.findById(99)).thenReturn(Optional.empty());

        WorkOrderNotFoundException ex = assertThrows(
                WorkOrderNotFoundException.class,
                () -> workOrderService.fetchWorkOrderById(99));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ───────────────────────── fetchWorkOrdersByTechnician ─────────────────────────

    @Test
    @DisplayName("fetchWorkOrdersByTechnician returns matching work orders")
    void fetchWorkOrdersByTechnician_returnsList() {
        when(workOrderRepository.findByTechnicianId(3)).thenReturn(List.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.fetchWorkOrdersByTechnician(3);

        assertEquals(List.of(sampleWorkOrder), response.get("data"));
    }

    @Test
    @DisplayName("fetchWorkOrdersByTechnician returns an empty list when none match")
    void fetchWorkOrdersByTechnician_returnsEmptyList() {
        when(workOrderRepository.findByTechnicianId(8)).thenReturn(Collections.emptyList());

        Map<String, Object> response = workOrderService.fetchWorkOrdersByTechnician(8);

        assertTrue(((List<?>) response.get("data")).isEmpty());
    }

    // ───────────────────────── updateWorkOrderStatus ─────────────────────────

    @Test
    @DisplayName("updateWorkOrderStatus SC -> IP stamps actualVisitDate")
    void updateWorkOrderStatus_SCtoIP_setsActualVisitDate() {
        sampleWorkOrder.setStatus("SC");
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.updateWorkOrderStatus(1, "IP");

        assertEquals("IP", sampleWorkOrder.getStatus());
        assertEquals(LocalDate.now(), sampleWorkOrder.getActualVisitDate());
        assertEquals("Work order status updated successfully", response.get("message"));
        verify(workOrderRepository).save(sampleWorkOrder);
    }

    @ParameterizedTest(name = "updateWorkOrderStatus allows valid transition {0} -> {1}")
    @CsvSource({"SC,IP", "IP,CM", "SC,CN", "IP,CN", "CM,CN"})
    void updateWorkOrderStatus_validTransitions(String from, String to) {
        sampleWorkOrder.setStatus(from);
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        workOrderService.updateWorkOrderStatus(1, to);

        assertEquals(to, sampleWorkOrder.getStatus());
        verify(workOrderRepository).save(sampleWorkOrder);
    }

    @ParameterizedTest(name = "updateWorkOrderStatus rejects invalid transition {0} -> {1}")
    @CsvSource({"SC,CM", "IP,SC", "CM,IP", "CM,SC", "CN,IP"})
    void updateWorkOrderStatus_invalidTransitions(String from, String to) {
        sampleWorkOrder.setStatus(from);
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        assertThrows(InvalidWorkOrderStatusException.class,
                () -> workOrderService.updateWorkOrderStatus(1, to));
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateWorkOrderStatus throws when work order not found")
    void updateWorkOrderStatus_notFound_throws() {
        when(workOrderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class,
                () -> workOrderService.updateWorkOrderStatus(99, "IP"));
    }

    // ───────────────────────── updateWorkOrderCosts ─────────────────────────

    @Test
    @DisplayName("updateWorkOrderCosts updates material and labour costs")
    void updateWorkOrderCosts_success() {
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.updateWorkOrderCosts(1, 750.0, 400.0);

        assertEquals(750.0, sampleWorkOrder.getMaterialCost());
        assertEquals(400.0, sampleWorkOrder.getLabourCost());
        assertEquals("Work order costs updated successfully", response.get("message"));
        verify(workOrderRepository).save(sampleWorkOrder);
    }

    @Test
    @DisplayName("updateWorkOrderCosts throws when work order not found")
    void updateWorkOrderCosts_notFound_throws() {
        when(workOrderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class,
                () -> workOrderService.updateWorkOrderCosts(99, 750.0, 400.0));
        verify(workOrderRepository, never()).save(any());
    }

    // ───────────────────────── completeWorkOrder ─────────────────────────

    @Test
    @DisplayName("completeWorkOrder sets CM and stamps actualVisitDate when it was null")
    void completeWorkOrder_nullVisitDate_setsToday() {
        sampleWorkOrder.setStatus("IP");
        sampleWorkOrder.setActualVisitDate(null);
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.completeWorkOrder(1);

        assertEquals("CM", sampleWorkOrder.getStatus());
        assertEquals(LocalDate.now(), sampleWorkOrder.getActualVisitDate());
        assertEquals("Work order completed successfully", response.get("message"));
        verify(workOrderRepository).save(sampleWorkOrder);
    }

    @Test
    @DisplayName("completeWorkOrder keeps an already-set actualVisitDate unchanged")
    void completeWorkOrder_existingVisitDate_unchanged() {
        LocalDate existing = LocalDate.of(2024, 3, 1);
        sampleWorkOrder.setStatus("IP");
        sampleWorkOrder.setActualVisitDate(existing);
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        workOrderService.completeWorkOrder(1);

        assertEquals("CM", sampleWorkOrder.getStatus());
        assertEquals(existing, sampleWorkOrder.getActualVisitDate());
    }

    @Test
    @DisplayName("completeWorkOrder throws when work order not found")
    void completeWorkOrder_notFound_throws() {
        when(workOrderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class,
                () -> workOrderService.completeWorkOrder(99));
        verify(workOrderRepository, never()).save(any());
    }

    // ───────────────────────── cancelWorkOrder (soft delete) ─────────────────────────

    @Test
    @DisplayName("cancelWorkOrder soft-deletes a Scheduled work order by setting status to CN")
    void cancelWorkOrder_fromScheduled_setsCN() {
        sampleWorkOrder.setStatus("SC");
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        Map<String, Object> response = workOrderService.cancelWorkOrder(1);

        assertEquals("CN", sampleWorkOrder.getStatus());
        assertEquals("Work order cancelled successfully", response.get("message"));
        verify(workOrderRepository).save(sampleWorkOrder);
    }

    @ParameterizedTest(name = "cancelWorkOrder rejects cancellation when status is {0}")
    @CsvSource({"IP", "CM", "CN"})
    void cancelWorkOrder_notScheduled_throws(String currentStatus) {
        sampleWorkOrder.setStatus(currentStatus);
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(sampleWorkOrder));

        InvalidWorkOrderStatusException ex = assertThrows(
                InvalidWorkOrderStatusException.class,
                () -> workOrderService.cancelWorkOrder(1));
        assertTrue(ex.getMessage().contains(currentStatus));
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelWorkOrder throws when work order not found")
    void cancelWorkOrder_notFound_throws() {
        when(workOrderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(WorkOrderNotFoundException.class,
                () -> workOrderService.cancelWorkOrder(99));
        verify(workOrderRepository, never()).save(any());
    }
}
