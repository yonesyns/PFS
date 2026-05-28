package com.fleet.payment.repository;

import com.fleet.payment.entity.Invoice;
import com.fleet.payment.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate < :date")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate BETWEEN :start AND :end")
    List<Invoice> findInvoicesDueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    long countByCustomerIdAndStatus(UUID customerId, InvoiceStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
}
