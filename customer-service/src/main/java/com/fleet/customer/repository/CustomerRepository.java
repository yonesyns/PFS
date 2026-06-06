package com.fleet.customer.repository;

import com.fleet.customer.entity.Customer;
import com.fleet.customer.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByVatNumber(String vatNumber);

    boolean existsByEmail(String email);

    boolean existsByVatNumber(String vatNumber);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:companyName = '' OR LOWER(c.companyName) LIKE CONCAT('%', :companyName, '%')) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:vatNumber = '' OR LOWER(c.vatNumber) LIKE CONCAT('%', :vatNumber, '%')) AND " +
           "(:email = '' OR LOWER(c.email) LIKE CONCAT('%', :email, '%')) AND " +
           "c.status != com.fleet.customer.entity.CustomerStatus.DELETED")
    Page<Customer> searchCustomers(
            @Param("companyName") String companyName,
            @Param("status") CustomerStatus status,
            @Param("vatNumber") String vatNumber,
            @Param("email") String email,
            Pageable pageable);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.status = 'PENDING'")
    Page<Customer> findPendingCustomers(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    long countByStatus(@Param("status") CustomerStatus status);
}
