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
public class SubscriptionCreatedEvent extends BaseEvent<SubscriptionCreatedEvent.SubscriptionPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class SubscriptionPayload {
        private UUID subscriptionId;
        private UUID customerId;
        private String planType;
    }
}
