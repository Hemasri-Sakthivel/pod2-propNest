package com.cog.propNest.module.maintenanceFacility.service;

import com.cog.propNest.module.maintenanceFacility.entity.MaintenanceRequest;
import com.cog.propNest.module.maintenanceFacility.exception.InvalidRequestStatusException;
import com.cog.propNest.module.maintenanceFacility.exception.MaintenanceRequestNotFoundException;
import com.cog.propNest.module.maintenanceFacility.repository.MaintenanceRequestRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MaintenanceRequestService} using JUnit 5 + Mockito.
 * The repository is mocked; no Spring context or database is required.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceTest {

    @Mock
    private MaintenanceRequestRepository requestRepository;

    @InjectMocks
    private MaintenanceRequestService requestService;

    private MaintenanceRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new MaintenanceRequest();
        sampleRequest.setRequestId(1);
        sampleRequest.setUnitId(1);
        sampleRequest.setAssignedTechId(3);
        sampleRequest.setCategory("PL");
        sampleRequest.setDescription("Water leakage in bathroom");
        sampleRequest.setPriority("HI");
        sampleRequest.setStatus("OP");
        sampleRequest.setRaisedDate(LocalDate.now());
    }

    // ───────────────────────── createRequest ─────────────────────────

    @Test
    @DisplayName("createRequest sets status to OP, stamps raisedDate, and saves")
    void createRequest_setsDefaultsAndSaves() {
        MaintenanceRequest incoming = new MaintenanceRequest();
        incoming.setUnitId(1);
        incoming.setCategory("PL");
        incoming.setPriority("HI");
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> response = requestService.createRequest(incoming);

        ArgumentCaptor<MaintenanceRequest> captor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(requestRepository).save(captor.capture());
        MaintenanceRequest saved = captor.getValue();
        assertEquals("OP", saved.getStatus());
        assertEquals(LocalDate.now(), saved.getRaisedDate());
        assertEquals("Maintenance request created successfully", response.get("message"));
        // createRequest returns a confirmation message only (no data payload)
        assertNull(response.get("data"));
    }

    // ───────────────────────── fetchAllRequests ─────────────────────────

    @Test
    @DisplayName("fetchAllRequests returns the list of requests")
    void fetchAllRequests_returnsList() {
        List<MaintenanceRequest> list = Arrays.asList(sampleRequest, new MaintenanceRequest());
        when(requestRepository.findAll()).thenReturn(list);

        Map<String, Object> response = requestService.fetchAllRequests();

        assertEquals(list, response.get("data"));
        verify(requestRepository).findAll();
    }

    @Test
    @DisplayName("fetchAllRequests returns an empty list when none exist")
    void fetchAllRequests_returnsEmptyList() {
        when(requestRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> response = requestService.fetchAllRequests();

        assertTrue(((List<?>) response.get("data")).isEmpty());
    }

    // ───────────────────────── fetchRequestById ─────────────────────────

    @Test
    @DisplayName("fetchRequestById returns the request when found")
    void fetchRequestById_found() {
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        Map<String, Object> response = requestService.fetchRequestById(1);

        assertEquals(sampleRequest, response.get("data"));
    }

    @Test
    @DisplayName("fetchRequestById throws MaintenanceRequestNotFoundException when missing")
    void fetchRequestById_notFound_throws() {
        when(requestRepository.findById(99)).thenReturn(Optional.empty());

        MaintenanceRequestNotFoundException ex = assertThrows(
                MaintenanceRequestNotFoundException.class,
                () -> requestService.fetchRequestById(99));
        assertTrue(ex.getMessage().contains("99"));
    }

    // ───────────────────────── fetchRequestsByUnit ─────────────────────────

    @Test
    @DisplayName("fetchRequestsByUnit returns matching requests")
    void fetchRequestsByUnit_returnsList() {
        when(requestRepository.findByUnitId(1)).thenReturn(List.of(sampleRequest));

        Map<String, Object> response = requestService.fetchRequestsByUnit(1);

        assertEquals(List.of(sampleRequest), response.get("data"));
    }

    @Test
    @DisplayName("fetchRequestsByUnit returns an empty list when no requests for the unit")
    void fetchRequestsByUnit_returnsEmptyList() {
        when(requestRepository.findByUnitId(50)).thenReturn(Collections.emptyList());

        Map<String, Object> response = requestService.fetchRequestsByUnit(50);

        assertTrue(((List<?>) response.get("data")).isEmpty());
    }

    // ───────────────────────── fetchRequestsByTenant ─────────────────────────

    @Test
    @DisplayName("fetchRequestsByTenant returns requests for the assigned technician")
    void fetchRequestsByTenant_returnsList() {
        when(requestRepository.findByAssignedTechId(3)).thenReturn(List.of(sampleRequest));

        Map<String, Object> response = requestService.fetchRequestsByTenant(3);

        assertEquals(List.of(sampleRequest), response.get("data"));
    }

    @Test
    @DisplayName("fetchRequestsByTenant returns an empty list when none match")
    void fetchRequestsByTenant_returnsEmptyList() {
        when(requestRepository.findByAssignedTechId(8)).thenReturn(Collections.emptyList());

        Map<String, Object> response = requestService.fetchRequestsByTenant(8);

        assertTrue(((List<?>) response.get("data")).isEmpty());
    }

    // ───────────────────────── assignTechnician ─────────────────────────

    @Test
    @DisplayName("assignTechnician sets tech id, moves status to AS, and saves")
    void assignTechnician_success() {
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        Map<String, Object> response = requestService.assignTechnician(1, 7);

        assertEquals(7, sampleRequest.getAssignedTechId());
        assertEquals("AS", sampleRequest.getStatus());
        assertEquals("Technician assigned successfully", response.get("message"));
        verify(requestRepository).save(sampleRequest);
    }

    @Test
    @DisplayName("assignTechnician throws when request not found")
    void assignTechnician_notFound_throws() {
        when(requestRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRequestNotFoundException.class,
                () -> requestService.assignTechnician(99, 7));
        verify(requestRepository, never()).save(any());
    }

    // ───────────────────────── updateStatus ─────────────────────────

    @ParameterizedTest(name = "updateStatus allows valid transition {0} -> {1}")
    @CsvSource({"OP,AS", "AS,IP", "IP,RS", "RS,CL", "CL,RO"})
    void updateStatus_validTransitions(String from, String to) {
        sampleRequest.setStatus(from);
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        requestService.updateStatus(1, to);

        assertEquals(to, sampleRequest.getStatus());
        verify(requestRepository).save(sampleRequest);
    }

    @ParameterizedTest(name = "updateStatus rejects invalid transition {0} -> {1}")
    @CsvSource({"OP,IP", "OP,CL", "AS,RS", "IP,AS", "CL,IP"})
    void updateStatus_invalidTransitions(String from, String to) {
        sampleRequest.setStatus(from);
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        assertThrows(InvalidRequestStatusException.class,
                () -> requestService.updateStatus(1, to));
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus to RS stamps resolvedDate")
    void updateStatus_toResolved_setsResolvedDate() {
        sampleRequest.setStatus("IP");
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        requestService.updateStatus(1, "RS");

        assertEquals("RS", sampleRequest.getStatus());
        assertEquals(LocalDate.now(), sampleRequest.getResolvedDate());
    }

    @Test
    @DisplayName("updateStatus throws when request not found")
    void updateStatus_notFound_throws() {
        when(requestRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRequestNotFoundException.class,
                () -> requestService.updateStatus(99, "AS"));
    }

    // ───────────────────────── closeRequest ─────────────────────────

    @Test
    @DisplayName("closeRequest sets status to CL and saves")
    void closeRequest_success() {
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        Map<String, Object> response = requestService.closeRequest(1);

        assertEquals("CL", sampleRequest.getStatus());
        assertEquals("Request closed successfully", response.get("message"));
        verify(requestRepository).save(sampleRequest);
    }

    @Test
    @DisplayName("closeRequest throws when request not found")
    void closeRequest_notFound_throws() {
        when(requestRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRequestNotFoundException.class,
                () -> requestService.closeRequest(99));
        verify(requestRepository, never()).save(any());
    }

    // ───────────────────────── reopenRequest ─────────────────────────

    @Test
    @DisplayName("reopenRequest sets status to RO and saves")
    void reopenRequest_success() {
        sampleRequest.setStatus("CL");
        when(requestRepository.findById(1)).thenReturn(Optional.of(sampleRequest));

        Map<String, Object> response = requestService.reopenRequest(1);

        assertEquals("RO", sampleRequest.getStatus());
        assertEquals("Request reopened successfully", response.get("message"));
        verify(requestRepository).save(sampleRequest);
    }

    @Test
    @DisplayName("reopenRequest throws when request not found")
    void reopenRequest_notFound_throws() {
        when(requestRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRequestNotFoundException.class,
                () -> requestService.reopenRequest(99));
        verify(requestRepository, never()).save(any());
    }
}
