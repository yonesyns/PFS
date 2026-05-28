package com.fleet.payment.dto;

import com.fleet.payment.entity.PlanType;
import com.fleet.payment.entity.SubscriptionStatus;
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
public class SubscriptionResponse {
    private UUID id;
    private UUID customerId;
    private PlanType planType;
    private Long monthlyAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private Boolean autoRenew;
    private LocalDate lastRenewalDate;
    private Instant createdAt;
    private Instant updatedAt;
}
