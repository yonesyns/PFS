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
public class VehicleCreatedEvent extends BaseEvent<VehicleCreatedEvent.VehiclePayload> {

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class VehiclePayload {
        private UUID vehicleId;
        private String plateNumber;
        private String brand;
        private String model;
        private UUID customerId;
    }
}
