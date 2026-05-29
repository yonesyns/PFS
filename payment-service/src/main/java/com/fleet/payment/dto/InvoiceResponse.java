package com.fleet.payment.dto;

import com.fleet.payment.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private UUID vehicleId;
    private UUID subscriptionId;
    private Long amount;
    private String currency;
    private String description;
    private InvoiceStatus status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}
