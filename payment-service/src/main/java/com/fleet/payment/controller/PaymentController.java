package com.fleet.payment.controller;

import com.fleet.commons.dto.ApiResponse;
import com.fleet.commons.dto.PageResponse;
import com.fleet.payment.dto.*;
import com.fleet.payment.entity.InvoiceStatus;
import com.fleet.payment.entity.PlanType;
import com.fleet.payment.service.PaymentService;
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
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing invoices, subscriptions and transactions")
public class PaymentController {

    private final PaymentService paymentService;

    // === INVOICE ENDPOINTS ===

    @PostMapping("/api/invoices")
    @Operation(summary = "Create a new invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = paymentService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invoice created successfully"));
    }

    @GetMapping("/api/invoices/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @PathVariable UUID id) {
        InvoiceResponse response = paymentService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/invoices")
    @Operation(summary = "Get all invoices")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getAllInvoices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<InvoiceResponse> response = paymentService.getAllInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/invoices/customer/{customerId}")
    @Operation(summary = "Get invoices by customer")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getInvoicesByCustomer(
            @PathVariable UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<InvoiceResponse> response = paymentService.getInvoicesByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/invoices/overdue")
    @Operation(summary = "Get overdue invoices")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getOverdueInvoices(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<InvoiceResponse> response = paymentService.getOverdueInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/invoices/{id}/pay")
    @Operation(summary = "Pay an invoice", description = "Simulate payment for MVP")
    public ResponseEntity<ApiResponse<InvoiceResponse>> payInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody PayInvoiceRequest request) {
        InvoiceResponse response = paymentService.payInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully"));
    }

    @PostMapping("/api/invoices/{id}/cancel")
    @Operation(summary = "Cancel an invoice")
    public ResponseEntity<ApiResponse<Void>> cancelInvoice(
            @PathVariable UUID id,
            @RequestParam String reason) {
        paymentService.cancelInvoice(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice cancelled successfully"));
    }

    // === SUBSCRIPTION ENDPOINTS ===

    @GetMapping("/api/subscriptions")
    @Operation(summary = "Get all subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getAllSubscriptions() {
        List<SubscriptionResponse> response = paymentService.getAllSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/subscriptions/{id}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @PathVariable UUID id) {
        SubscriptionResponse response = paymentService.getSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/subscriptions")
    @Operation(summary = "Create a subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @RequestParam UUID customerId,
            @RequestParam PlanType planType,
            @RequestParam Long monthlyAmount) {
        SubscriptionResponse response = paymentService.createSubscription(customerId, planType, monthlyAmount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subscription created successfully"));
    }

    @PatchMapping("/api/subscriptions/{id}/upgrade")
    @Operation(summary = "Upgrade subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradeSubscription(
            @PathVariable UUID id,
            @RequestParam PlanType newPlan,
            @RequestParam Long newAmount) {
        SubscriptionResponse response = paymentService.upgradeSubscription(id, newPlan, newAmount);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription upgraded successfully"));
    }

    @PatchMapping("/api/subscriptions/{id}/cancel")
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable UUID id,
            @RequestParam String reason) {
        paymentService.cancelSubscription(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled successfully"));
    }

    // === TRANSACTION ENDPOINTS ===

    @GetMapping("/api/transactions")
    @Operation(summary = "Get transactions by invoice")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByInvoice(
            @RequestParam UUID invoiceId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<TransactionResponse> response = paymentService.getTransactionsByInvoice(invoiceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}