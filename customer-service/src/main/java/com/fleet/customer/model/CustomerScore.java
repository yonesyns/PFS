package com.fleet.customer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customer_scores")
@Data
public class CustomerScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    private int totalReservations;
    private int cancelledReservations;
    private int lateReturns;

    // Score de fiabilité calculé : 100 - (annulations * 10) - (retards * 5), min 0
    public int getReliabilityScore() {
        return Math.max(0, 100 - (cancelledReservations * 10) - (lateReturns * 5));
    }
}
