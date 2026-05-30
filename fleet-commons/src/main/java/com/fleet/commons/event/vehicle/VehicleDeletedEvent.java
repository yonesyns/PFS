package com.fleet.commons.event.vehicle;

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
public class VehicleDeletedEvent extends BaseEvent<VehicleDeletedEvent.VehicleDeletedPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class VehicleDeletedPayload {
        private UUID vehicleId;
        private UUID customerId;
    }
}
