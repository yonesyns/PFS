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
public class CustomerDeletedEvent extends BaseEvent<CustomerDeletedEvent.CustomerDeletedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerDeletedPayload {
        private UUID customerId;
        private UUID deletedBy;
    }
}
