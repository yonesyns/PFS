package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.MaintenanceStatus;
import com.fleet.vehicle.entity.MaintenanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceResponse {

    private UUID id;
    private UUID vehicleId;
    private String vehiclePlateNumber;
    private MaintenanceType type;
    private String description;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private MaintenanceStatus status;
    private Long estimatedCost;
    private Long actualCost;
    private Long mileageAtMaintenance;
    private String garageName;
    private String garageContact;
    private String partsUsed;
    private String technicianName;
    private String notes;
    private String cancellationReason;
    private boolean overdue;
    private Instant createdAt;
    private Instant updatedAt;
}