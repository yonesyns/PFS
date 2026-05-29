package com.fleet.payment.dto;

import com.fleet.payment.entity.PaymentMethod;
import com.fleet.payment.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID invoiceId;
    private Long amount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String externalReference;
    private Instant transactionDate;
    private String failureReason;
    private Instant createdAt;
}
