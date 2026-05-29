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
public class VehicleStatusChangedEvent extends BaseEvent<VehicleStatusChangedEvent.VehicleStatusPayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class VehicleStatusPayload {
        private UUID vehicleId;
        private String oldStatus;
        private String newStatus;
        private String reason;
    }
}
