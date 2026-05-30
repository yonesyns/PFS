package com.fleet.vehicle.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.vehicle.dto.*;
import com.fleet.vehicle.entity.MaintenancePlan;
import com.fleet.vehicle.entity.MaintenanceStatus;
import com.fleet.vehicle.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Maintenance Management", description = "APIs for vehicle maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // === MAINTENANCE ENDPOINTS ===

    @PostMapping("/api/vehicles/{vehicleId}/maintenance")
    @Operation(summary = "Schedule maintenance for a vehicle")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> scheduleMaintenance(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody MaintenanceRequest request) {
        MaintenanceResponse response = maintenanceService.scheduleMaintenance(vehicleId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Maintenance scheduled successfully"));
    }

    @PatchMapping("/api/maintenance/{maintenanceId}/start")
    @Operation(summary = "Start a scheduled maintenance")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> startMaintenance(
            @PathVariable UUID maintenanceId) {
        MaintenanceResponse response = maintenanceService.startMaintenance(maintenanceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Maintenance started"));
    }

    @PatchMapping("/api/maintenance/{maintenanceId}/complete")
    @Operation(summary = "Complete an in-progress maintenance")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> completeMaintenance(
            @PathVariable UUID maintenanceId,
            @Valid @RequestBody MaintenanceCompletionRequest request) {
        MaintenanceResponse response = maintenanceService.completeMaintenance(maintenanceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Maintenance completed successfully"));
    }

    @PatchMapping("/api/maintenance/{maintenanceId}/cancel")
    @Operation(summary = "Cancel a maintenance")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> cancelMaintenance(
            @PathVariable UUID maintenanceId,
            @RequestParam String reason) {
        MaintenanceResponse response = maintenanceService.cancelMaintenance(maintenanceId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Maintenance cancelled"));
    }

    @GetMapping("/api/vehicles/{vehicleId}/maintenance")
    @Operation(summary = "Get maintenance history for a vehicle")
    public ResponseEntity<ApiResponse<List<MaintenanceResponse>>> getMaintenanceHistory(
            @PathVariable UUID vehicleId) {
        List<MaintenanceResponse> response = maintenanceService.getMaintenanceHistory(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/maintenance/status/{status}")
    @Operation(summary = "Get maintenances by status")
    public ResponseEntity<ApiResponse<PageResponse<MaintenanceResponse>>> getMaintenancesByStatus(
            @PathVariable MaintenanceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<MaintenanceResponse> response = maintenanceService.getMaintenancesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/maintenance/upcoming")
    @Operation(summary = "Get upcoming maintenances (next X days)")
    public ResponseEntity<ApiResponse<List<MaintenanceAlertResponse>>> getUpcomingMaintenances(
            @RequestParam(defaultValue = "30") int days) {
        List<MaintenanceAlertResponse> response = maintenanceService.getUpcomingMaintenances(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/maintenance/overdue")
    @Operation(summary = "Get overdue maintenances")
    public ResponseEntity<ApiResponse<List<MaintenanceAlertResponse>>> getOverdueMaintenances() {
        List<MaintenanceAlertResponse> response = maintenanceService.getOverdueMaintenances();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // === MAINTENANCE PLAN ENDPOINTS ===

    @PostMapping("/api/vehicles/{vehicleId}/maintenance-plan")
    @Operation(summary = "Create a preventive maintenance plan")
    public ResponseEntity<ApiResponse<MaintenancePlan>> createMaintenancePlan(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody MaintenancePlanRequest request) {
        MaintenancePlan response = maintenanceService.createMaintenancePlan(vehicleId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Maintenance plan created"));
    }

    @GetMapping("/api/vehicles/{vehicleId}/maintenance-plan")
    @Operation(summary = "Get maintenance plans for a vehicle")
    public ResponseEntity<ApiResponse<List<MaintenancePlan>>> getMaintenancePlans(
            @PathVariable UUID vehicleId) {
        List<MaintenancePlan> response = maintenanceService.getMaintenancePlans(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/maintenance-plan/{planId}/deactivate")
    @Operation(summary = "Deactivate a maintenance plan")
    public ResponseEntity<ApiResponse<Void>> deactivateMaintenancePlan(
            @PathVariable UUID planId) {
        maintenanceService.deactivateMaintenancePlan(planId);
        return ResponseEntity.ok(ApiResponse.success(null, "Maintenance plan deactivated"));
    }
}