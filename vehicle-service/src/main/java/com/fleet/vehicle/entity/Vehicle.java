package com.fleet.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicles_plate", columnList = "plate_number", unique = true),
    @Index(name = "idx_vehicles_vin", columnList = "vin", unique = true),
    @Index(name = "idx_vehicles_customer", columnList = "customer_id"),
    @Index(name = "idx_vehicles_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "plate_number", nullable = false, length = 20, unique = true)
    private String plateNumber;

    @Column(nullable = false, length = 17, unique = true)
    private String vin;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 20)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FuelType fuelType = FuelType.DIESEL;

    @Column
    private Long mileage;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "technical_inspection_date")
    private LocalDate technicalInspectionDate;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "gps_device_id", length = 50)
    private String gpsDeviceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
