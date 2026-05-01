package com.fleet.payment.repository;

import com.fleet.payment.model.Payment;
import com.fleet.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerId(Long customerId);
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByStatus(PaymentStatus status);
}