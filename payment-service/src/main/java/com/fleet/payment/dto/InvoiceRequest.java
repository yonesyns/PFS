package com.fleet.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID vehicleId;
    private UUID subscriptionId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private Long amount;

    private String currency = "EUR";

    @NotBlank(message = "Description is required")
    private String description;
}
