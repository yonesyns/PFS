package com.fleet.payment.dto;

import com.fleet.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayInvoiceRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
