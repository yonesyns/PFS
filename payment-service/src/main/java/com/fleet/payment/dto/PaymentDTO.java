package com.fleet.payment.dto;

import com.fleet.payment.model.PaymentMethod;
import com.fleet.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private Long id;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private PaymentStatus status;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
    
    private String transactionId;
    private String description;
}