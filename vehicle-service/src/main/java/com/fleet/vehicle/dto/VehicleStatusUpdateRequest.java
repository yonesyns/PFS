package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private VehicleStatus status;

    private String reason;
}
