package com.fleet.payment.dto;

import com.fleet.payment.entity.PlanType;
import jakarta.validation.constraints.Min;
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
public class SubscriptionRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Plan type is required")
    private PlanType planType;

    @NotNull(message = "Monthly amount is required")
    @Min(value = 0, message = "Monthly amount must be positive")
    private Long monthlyAmount;

    private Boolean autoRenew = true;
}
