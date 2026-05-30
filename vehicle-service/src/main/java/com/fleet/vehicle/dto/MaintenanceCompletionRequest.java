package com.fleet.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCompletionRequest {

    @NotNull(message = "Completion date is required")
    private LocalDate completedDate;

    @Min(value = 0, message = "Actual cost cannot be negative")
    private Long actualCost;  // en centimes

    @NotNull(message = "Mileage at maintenance is required")
    @Min(value = 0, message = "Mileage cannot be negative")
    private Long mileageAtMaintenance;

    private String partsUsed;

    private String notes;
}