package com.fleet.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email", unique = true),
    @Index(name = "idx_customers_vat", columnList = "vat_number", unique = true),
    @Index(name = "idx_customers_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 50, unique = true)
    private String vatNumber;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Embedded
    private Address address;

    @Column(length = 50)
    private String contactFirstName;

    @Column(length = 50)
    private String contactLastName;

    @Column(length = 100)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.PENDING;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.status == null) {
            this.status = CustomerStatus.PENDING;
        }
    }

    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == CustomerStatus.DELETED;
    }
}
