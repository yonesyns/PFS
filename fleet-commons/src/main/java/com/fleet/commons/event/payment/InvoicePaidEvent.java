package com.fleet.commons.event.payment;

import com.fleet.commons.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class InvoicePaidEvent extends BaseEvent<InvoicePaidEvent.InvoicePaidPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class InvoicePaidPayload {
        private UUID invoiceId;
        private UUID customerId;
        private Long amount;
        private String paymentMethod;
    }
}
