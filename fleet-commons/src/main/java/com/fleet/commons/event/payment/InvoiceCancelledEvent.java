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
public class InvoiceCancelledEvent extends BaseEvent<InvoiceCancelledEvent.InvoiceCancelledPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class InvoiceCancelledPayload {
        private UUID invoiceId;
        private UUID customerId;
        private String reason;
    }
}
