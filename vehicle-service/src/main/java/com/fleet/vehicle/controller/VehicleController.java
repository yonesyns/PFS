package com.fleet.vehicle.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.vehicle.dto.*;
import com.fleet.vehicle.entity.VehicleStatus;
import com.fleet.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "APIs for managing fleet vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Create a new vehicle")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Vehicle created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable UUID id) {
        VehicleResponse response = vehicleService.getVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Returns paginated list of all vehicles")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getAllVehicles(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<VehicleResponse> response = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "Get vehicles by customer")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getVehiclesByCustomer(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<VehicleResponse> response = vehicleService.getVehiclesByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getVehiclesByStatus(
            @PathVariable VehicleStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<VehicleResponse> response = vehicleService.getVehiclesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring-insurance")
    @Operation(summary = "Get vehicles with expiring insurance")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getExpiringInsurance(
            @RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<VehicleResponse> response = vehicleService.getExpiringInsurance(days, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/maintenance-due")
    @Operation(summary = "Get vehicles with maintenance due")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMaintenanceDue() {
        List<VehicleResponse> response = vehicleService.getMaintenanceDue();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update vehicle status")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleStatusUpdateRequest request) {
        VehicleResponse response = vehicleService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Status updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign vehicle to customer")
    public ResponseEntity<ApiResponse<VehicleResponse>> assignVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleAssignRequest request) {
        VehicleResponse response = vehicleService.assignVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle assigned successfully"));
    }

    @PatchMapping("/{id}/unassign")
    @Operation(summary = "Unassign vehicle from customer")
    public ResponseEntity<ApiResponse<VehicleResponse>> unassignVehicle(
            @PathVariable UUID id) {
        VehicleResponse response = vehicleService.unassignVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle unassigned successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Vehicle deleted successfully"));
    }
}