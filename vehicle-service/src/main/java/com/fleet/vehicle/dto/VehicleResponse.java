package com.fleet.vehicle.dto;

import com.fleet.vehicle.entity.FuelType;
import com.fleet.vehicle.entity.VehicleStatus;
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
public class VehicleResponse {
    private UUID id;
    private String plateNumber;
    private String vin;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private VehicleStatus status;
    private FuelType fuelType;
    private Long mileage;
    private LocalDate registrationDate;
    private LocalDate insuranceExpiryDate;
    private LocalDate technicalInspectionDate;
    private UUID customerId;
    private String gpsDeviceId;
    private Instant createdAt;
    private Instant updatedAt;
}
