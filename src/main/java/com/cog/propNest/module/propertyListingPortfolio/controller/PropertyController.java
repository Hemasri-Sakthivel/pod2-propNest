package com.cog.propNest.module
    .propertyListingPortfolio.controller;

import com.cog.propNest.module
    .propertyListingPortfolio.dto.request
    .PropertyRequestDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.dto.response
    .PropertyResponseDTO;
import com.cog.propNest.module
    .propertyListingPortfolio.service
    .PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/propNest/propertyListing")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // ── POST ──────────────────────────────────
    @PostMapping("/createProperty")
    public ResponseEntity<?> createProperty(
            @RequestBody PropertyRequestDTO dto) {
        try {
            propertyService.createProperty(dto);
            return ResponseEntity.status(201).body(
                Map.of("message",
                    "Property created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── GET All ───────────────────────────────
    @GetMapping("/fetchAllProperties")
    public ResponseEntity<?> fetchAllProperties() {
        List<PropertyResponseDTO> list =
            propertyService.getAllProperties();
        if (list.isEmpty())
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No properties found"));
        return ResponseEntity.ok(list);
    }

    // ── GET by ID ─────────────────────────────
    @GetMapping("/fetchPropertyById/{propertyId}")
    public ResponseEntity<?> fetchPropertyById(
            @PathVariable Integer propertyId) {
        PropertyResponseDTO dto =
            propertyService.getPropertyById(
                propertyId);
        if (dto == null)
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "Property not found"));
        return ResponseEntity.ok(dto);
    }

    // ── GET by Owner ──────────────────────────
    @GetMapping(
        "/fetchPropertiesByOwner/{ownerId}")
    public ResponseEntity<?> fetchByOwner(
            @PathVariable Long ownerId) {
        List<PropertyResponseDTO> list =
            propertyService
                .getPropertiesByOwner(ownerId);
        if (list.isEmpty())
            return ResponseEntity.status(404).body(
                Map.of("message",
                    "No properties found " +
                    "for given owner"));
        return ResponseEntity.ok(list);
    }

    // ── GET by Type ───────────────────────────
    @GetMapping(
        "/fetchPropertiesByType/{type}")
    public ResponseEntity<?> fetchByType(
            @PathVariable String type) {
        try {
            List<PropertyResponseDTO> list =
                propertyService
                    .getPropertiesByType(type);
            if (list.isEmpty())
                return ResponseEntity.status(404)
                    .body(Map.of("message",
                        "No properties found " +
                        "for given type"));
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── GET by City ───────────────────────────
    @GetMapping(
        "/fetchPropertiesByCity/{city}")
    public ResponseEntity<?> fetchByCity(
            @PathVariable String city) {
        try {
            List<PropertyResponseDTO> list =
                propertyService
                    .getPropertiesByCity(city);
            if (list.isEmpty())
                return ResponseEntity.status(404)
                    .body(Map.of("message",
                        "No properties found " +
                        "for given city"));
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Update ────────────────────────────
    @PutMapping("/updateProperty/{propertyId}")
    public ResponseEntity<?> updateProperty(
            @PathVariable Integer propertyId,
            @RequestBody PropertyRequestDTO dto) {
        try {
            propertyService.updateProperty(
                propertyId, dto);
            return ResponseEntity.ok(Map.of(
                "message",
                "Property updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Update Status ─────────────────────
    @PutMapping(
        "/updatePropertyStatus/{propertyId}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer propertyId,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null ||
                newStatus.isBlank())
                return ResponseEntity.status(400)
                    .body(Map.of("message",
                        "status is required"));
            propertyService.updatePropertyStatus(
                propertyId, newStatus);
            return ResponseEntity.ok(Map.of(
                "message",
                "Property status updated " +
                "successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(
                Map.of("message", e.getMessage()));
        }
    }

    // ── PUT Delist ────────────────────────────
    @PutMapping("/delistProperty/{propertyId}")
    public ResponseEntity<?> delistProperty(
            @PathVariable Integer propertyId) {
        try {
            propertyService.delistProperty(
                propertyId);
            return ResponseEntity.ok(Map.of(
                "message",
                "Property delisted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(
                Map.of("message", e.getMessage()));
        }
    }
}
