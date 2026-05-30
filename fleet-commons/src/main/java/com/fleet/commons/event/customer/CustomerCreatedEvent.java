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
public class CustomerCreatedEvent extends BaseEvent<CustomerCreatedEvent.CustomerPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerPayload {
        private UUID customerId;
        private String companyName;
        private String email;
        private String vatNumber;
    }
}
