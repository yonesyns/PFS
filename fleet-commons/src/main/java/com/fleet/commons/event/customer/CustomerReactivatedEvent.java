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
public class CustomerReactivatedEvent extends BaseEvent<CustomerReactivatedEvent.CustomerReactivatedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerReactivatedPayload {
        private UUID customerId;
        private UUID reactivatedBy;
    }
}
