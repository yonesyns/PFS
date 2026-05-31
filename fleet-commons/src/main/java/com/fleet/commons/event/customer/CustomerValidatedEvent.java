package com.fleet.commons.event.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerValidatedEvent {
    private String eventType;
    private String source;
    private CustomerValidatedPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerValidatedPayload {
        private UUID customerId;
        private UUID validatedBy;
        private String companyName;
        private String email;
    }
}