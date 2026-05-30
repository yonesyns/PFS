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
@Table(name = "maintenance_plans", indexes = {
        @Index(name = "idx_mplans_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_mplans_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePlan {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "interval_type", nullable = false, length = 20)
    private IntervalType intervalType;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;  // km ou mois

    @Column(name = "last_done_date")
    private LocalDate lastDoneDate;

    @Column(name = "last_done_mileage")
    private Long lastDoneMileage;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "next_due_mileage")
    private Long nextDueMileage;

    @Column(name = "alert_days_before")
    @Builder.Default
    private Integer alertDaysBefore = 30;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public enum IntervalType {
        KM_BASED,    // Tous les X kilomètres
        TIME_BASED   // Tous les X mois
    }

    public boolean isDueBasedOnDate() {
        if (!isActive || nextDueDate == null) return false;
        return !LocalDate.now().plusDays(alertDaysBefore).isBefore(nextDueDate);
    }

    public boolean isDueBasedOnMileage(Long currentMileage) {
        if (!isActive || nextDueMileage == null || currentMileage == null) return false;
        return currentMileage >= nextDueMileage;
    }
}