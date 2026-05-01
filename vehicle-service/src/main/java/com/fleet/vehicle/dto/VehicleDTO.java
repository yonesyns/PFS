package com.fleet.vehicle.dto;

import com.fleet.vehicle.model.VehicleStatus;
import com.fleet.vehicle.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDTO {
    private Long id;
    
    @NotBlank(message = "VIN is required")
    private String vin;
    
    @NotBlank(message = "Make is required")
    private String make;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    private String color;
    private String licensePlate;
    private VehicleStatus status;
    private VehicleType type;
    private Double mileage;
    private String location;
    private Long customerId;
}