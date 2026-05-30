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
@Table(name = "maintenances", indexes = {
        @Index(name = "idx_maintenances_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_maintenances_status", columnList = "status"),
        @Index(name = "idx_maintenances_scheduled_date", columnList = "scheduled_date"),
        @Index(name = "idx_maintenances_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maintenance {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(name = "estimated_cost")
    private Long estimatedCost;  // en centimes

    @Column(name = "actual_cost")
    private Long actualCost;  // en centimes

    @Column(name = "mileage_at_maintenance")
    private Long mileageAtMaintenance;

    @Column(name = "garage_name", length = 100)
    private String garageName;

    @Column(name = "garage_contact", length = 200)
    private String garageContact;

    @Column(name = "parts_used", length = 1000)
    private String partsUsed;  // JSON ou texte simple: "Huile 5L, Filtre à air, Filtre à huile"

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(length = 2000)
    private String notes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean isOverdue() {
        return this.status == MaintenanceStatus.SCHEDULED
                && this.scheduledDate.isBefore(LocalDate.now());
    }
}