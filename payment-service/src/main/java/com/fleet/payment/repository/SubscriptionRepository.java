package com.fleet.payment.repository;

import com.fleet.payment.entity.Subscription;
import com.fleet.payment.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByCustomerIdAndStatus(UUID customerId, SubscriptionStatus status);

    List<Subscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :date")
    List<Subscription> findExpiringSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);
}
