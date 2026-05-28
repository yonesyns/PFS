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
public class InvoiceOverdueEvent extends BaseEvent<InvoiceOverdueEvent.InvoiceOverduePayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class InvoiceOverduePayload {
        private UUID invoiceId;
        private UUID customerId;
        private Long amount;
        private Integer daysOverdue;
    }
}
