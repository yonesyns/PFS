package com.fleet.commons.event.customer;

import com.fleet.commons.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class CustomerUpdatedEvent extends BaseEvent<CustomerUpdatedEvent.CustomerUpdatedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class CustomerUpdatedPayload {
        private UUID customerId;
        private List<String> fieldsChanged;
    }
}
