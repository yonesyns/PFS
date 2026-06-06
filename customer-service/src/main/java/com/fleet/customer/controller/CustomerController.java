package com.fleet.customer.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.customer.dto.*;
import com.fleet.customer.service.CustomerService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing B2B customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Self-registration, creates customer with PENDING status")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Customer created successfully. Waiting for admin validation."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {
        CustomerResponse response = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Admin only - returns paginated list")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CustomerResponse> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search by company name, status, VAT number, or email")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> searchCustomers(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String vatNumber,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        CustomerSearchRequest searchRequest = CustomerSearchRequest.builder()
                .companyName(companyName)
                .status(status != null && !status.isBlank() ? com.fleet.customer.entity.CustomerStatus.valueOf(status) : null)
                .vatNumber(vatNumber)
                .email(email)
                .build();

        PageResponse<CustomerResponse> response = customerService.searchCustomers(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending customers", description = "Admin only - customers awaiting validation")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getPendingCustomers(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<CustomerResponse> response = customerService.getPendingCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer updated successfully"));
    }

    @PatchMapping("/{id}/validate")
    @Operation(summary = "Validate a customer", description = "Admin only - activates a PENDING customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> validateCustomer(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID validatedBy) {
        CustomerResponse response = customerService.validateCustomer(id, validatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Customer validated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status", description = "Admin only - ACTIVE/INACTIVE/SUSPENDED/DELETED")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request,
            @RequestHeader("X-User-Id") UUID updatedBy) {
        CustomerResponse response = customerService.updateStatus(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Admin only - soft delete")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID deletedBy) {
        customerService.deleteCustomer(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
    }
}
