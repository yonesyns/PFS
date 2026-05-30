package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.MaintenanceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class MaintenanceRequest {

    @NotNull(message = "Maintenance type is required")
    private MaintenanceType type;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Min(value = 0, message = "Estimated cost cannot be negative")
    private Long estimatedCost;  // en centimes

    private String garageName;

    private String garageContact;

    private String partsUsed;

    private String technicianName;

    private String notes;
}