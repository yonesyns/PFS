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
public class CustomerValidatedEvent extends BaseEvent<CustomerValidatedEvent.CustomerValidatedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerValidatedPayload {
        private UUID customerId;
        private UUID validatedBy;
    }
}
