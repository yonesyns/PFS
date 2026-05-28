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
public class SubscriptionCancelledEvent extends BaseEvent<SubscriptionCancelledEvent.SubscriptionCancelledPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class SubscriptionCancelledPayload {
        private UUID subscriptionId;
        private UUID customerId;
        private String reason;
    }
}
