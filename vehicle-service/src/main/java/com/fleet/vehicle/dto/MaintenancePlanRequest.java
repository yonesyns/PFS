package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.MaintenancePlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Interval type is required")
    private MaintenancePlan.IntervalType intervalType;

    @NotNull(message = "Interval value is required")
    @Min(value = 1, message = "Interval must be at least 1")
    private Integer intervalValue;

    @Min(value = 1, message = "Alert days must be at least 1")
    private Integer alertDaysBefore = 30;
}