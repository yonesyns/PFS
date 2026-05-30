package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.MaintenanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceAlertResponse {

    private UUID maintenanceId;
    private UUID vehicleId;
    private String vehiclePlateNumber;
    private MaintenanceType type;
    private String description;
    private LocalDate scheduledDate;
    private Integer daysUntilDue;
    private Long overdueDays;
    private String alertLevel;  // INFO, WARNING, CRITICAL
    private String message;
}