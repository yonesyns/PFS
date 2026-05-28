package com.fleet.commons.event.customer;

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
public class CustomerSuspendedEvent extends BaseEvent<CustomerSuspendedEvent.CustomerSuspendedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerSuspendedPayload {
        private UUID customerId;
        private String reason;
        private UUID suspendedBy;
    }
}
