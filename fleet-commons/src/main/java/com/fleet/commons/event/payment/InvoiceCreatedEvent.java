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
public class InvoiceCreatedEvent extends BaseEvent<InvoiceCreatedEvent.InvoicePayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class InvoicePayload {
        private UUID invoiceId;
        private String invoiceNumber;
        private UUID customerId;
        private Long amount;
        private String currency;
    }
}
