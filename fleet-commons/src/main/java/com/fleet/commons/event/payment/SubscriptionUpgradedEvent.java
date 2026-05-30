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
public class SubscriptionUpgradedEvent extends BaseEvent<SubscriptionUpgradedEvent.SubscriptionUpgradedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class SubscriptionUpgradedPayload {
        private UUID subscriptionId;
        private UUID customerId;
        private String oldPlan;
        private String newPlan;
    }
}
