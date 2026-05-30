package com.fleet.vehicle.dto;

import com.fleet.commons.validation.ValidPlateNumber;
import com.fleet.commons.validation.ValidVin;
import com.fleet.vehicle.entity.FuelType;
import jakarta.validation.constraints.*;
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
public class VehicleRequest {

    @NotBlank(message = "Plate number is required")
    @ValidPlateNumber
    private String plateNumber;

    @NotBlank(message = "VIN is required")
    @ValidVin
    private String vin;

    @NotBlank(message = "Brand is required")
    @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be after 1900")
    @Max(value = 2100, message = "Year must be before 2100")
    private Integer year;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    private FuelType fuelType;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Long mileage;

    private LocalDate registrationDate;

    @Future(message = "Insurance expiry date must be in the future")
    private LocalDate insuranceExpiryDate;

    private LocalDate technicalInspectionDate;

    private UUID customerId;

    @Size(max = 50, message = "GPS device ID must not exceed 50 characters")
    private String gpsDeviceId;
}
