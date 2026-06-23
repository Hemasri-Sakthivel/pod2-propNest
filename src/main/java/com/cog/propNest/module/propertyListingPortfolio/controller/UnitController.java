package com.cog.propNest.module
    .propertyListingPortfolio.controller;

import com.cog.propNest.module
    .propertyListingPortfolio.dto.request
    .UnitRequestDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.dto.response
    .UnitResponseDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.service.UnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/propNest/propertyListing")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    // ── POST ──────────────────────────────────
    @PostMapping("/createUnit")
    public ResponseEntity<?> createUnit(
            @RequestBody UnitRequestDTO dto) {
        try {
            unitService.createUnit(dto);
            return ResponseEntity.status(201).body(
                Map.of("message",
                    "Unit created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── GET All ───────────────────────────────
    @GetMapping("/fetchAllUnits")
    public ResponseEntity<?> fetchAllUnits() {
        List<UnitResponseDTO> list =
            unitService.getAllUnits();
        if (list.isEmpty())
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No units found"));
        return ResponseEntity.ok(list);
    }

    // ── GET by ID ─────────────────────────────
    @GetMapping("/fetchUnitById/{unitId}")
    public ResponseEntity<?> fetchUnitById(
            @PathVariable Integer unitId) {
        UnitResponseDTO dto =
            unitService.getUnitById(unitId);
        if (dto == null)
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "Unit not found"));
        return ResponseEntity.ok(dto);
    }

    // ── GET by Property ───────────────────────
    @GetMapping(
        "/fetchUnitsByProperty/{propertyId}")
    public ResponseEntity<?> fetchByProperty(
            @PathVariable Integer propertyId) {
        List<UnitResponseDTO> list =
            unitService.getUnitsByProperty(
                propertyId);
        if (list.isEmpty())
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No units found " +
                    "for given property"));
        return ResponseEntity.ok(list);
    }

    // ── GET Vacant ────────────────────────────
    @GetMapping("/fetchVacantUnits")
    public ResponseEntity<?> fetchVacantUnits() {
        List<UnitResponseDTO> list =
            unitService.getVacantUnits();
        if (list.isEmpty())
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No vacant units found"));
        return ResponseEntity.ok(list);
    }

    // ── GET by Tenant ─────────────────────────
    @GetMapping(
        "/fetchUnitByTenant/{tenantId}")
    public ResponseEntity<?> fetchByTenant(
            @PathVariable Long tenantId) {
        UnitResponseDTO dto =
            unitService.getUnitByTenant(tenantId);
        if (dto == null)
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No unit found " +
                    "for given tenant"));
        return ResponseEntity.ok(dto);
    }

    // ── GET by Type and Furnishing ────────────
    @GetMapping(
        "/fetchUnitsByTypeAndFurnishing")
    public ResponseEntity<?> fetchByTypeAndFurnishing(
            @RequestParam String type,
            @RequestParam String furnishing) {
        try {
            List<UnitResponseDTO> list =
                unitService
                    .getUnitsByTypeAndFurnishing(
                        type, furnishing);
            if (list.isEmpty())
                return ResponseEntity.status(404)
                    .body(Map.of("message",
                        "No units found " +
                        "for given filters"));
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── GET by Rent Range ─────────────────────
    @GetMapping("/fetchUnitsByRentRange")
    public ResponseEntity<?> fetchByRentRange(
            @RequestParam Double minRent,
            @RequestParam Double maxRent) {
        try {
            List<UnitResponseDTO> list =
                unitService.getUnitsByRentRange(
                    minRent, maxRent);
            if (list.isEmpty())
                return ResponseEntity.status(404)
                    .body(Map.of("message",
                        "No units found " +
                        "in given range"));
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Assign Tenant ─────────────────────
    @PutMapping("/assignTenant/{unitId}")
    public ResponseEntity<?> assignTenant(
            @PathVariable Integer unitId,
            @RequestBody Map<String, Long> body) {
        try {
            unitService.assignTenant(
                unitId, body.get("tenantId"));
            return ResponseEntity.ok(Map.of(
                "message",
                "Tenant assigned successfully, " +
                "unit marked Occupied"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Vacate ────────────────────────────
    @PutMapping("/vacateUnit/{unitId}")
    public ResponseEntity<?> vacateUnit(
            @PathVariable Integer unitId) {
        try {
            unitService.vacateUnit(unitId);
            return ResponseEntity.ok(Map.of(
                "message",
                "Unit vacated successfully, " +
                "status set to Vacant"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Update Details ────────────────────
    @PutMapping("/updateUnitDetails/{unitId}")
    public ResponseEntity<?> updateUnitDetails(
            @PathVariable Integer unitId,
            @RequestBody UnitRequestDTO dto) {
        try {
            unitService.updateUnitDetails(
                unitId, dto);
            return ResponseEntity.ok(Map.of(
                "message",
                "Unit updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Update Status ─────────────────────
    @PutMapping("/updateUnitStatus/{unitId}")
    public ResponseEntity<?> updateUnitStatus(
            @PathVariable Integer unitId,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null ||
                newStatus.isBlank())
                return ResponseEntity.status(400)
                    .body(Map.of("message",
                        "status is required"));
            unitService.updateUnitStatus(
                unitId, newStatus);
            return ResponseEntity.ok(Map.of(
                "message",
                "Unit status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Bulk Update ───────────────────────
    @PutMapping(
        "/bulkUpdateUnitStatusByProperty" +
        "/{propertyId}")
    public ResponseEntity<?> bulkUpdate(
            @PathVariable Integer propertyId,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null ||
                newStatus.isBlank())
                return ResponseEntity.status(400)
                    .body(Map.of("message",
                        "status is required"));
            unitService.bulkUpdateStatus(
                propertyId, newStatus);
            return ResponseEntity.ok(Map.of(
                "message",
                "All units updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }
}
