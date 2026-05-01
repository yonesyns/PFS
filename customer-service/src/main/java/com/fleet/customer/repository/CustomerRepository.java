package com.fleet.customer.repository;

import com.fleet.customer.model.Customer;
import com.fleet.customer.model.CustomerStatus;
import com.fleet.customer.model.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("""
        SELECT c FROM Customer c WHERE
        (:name IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                      OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :name, '%'))) AND
        (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND
        (:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%')) AND
        (:status IS NULL OR c.status = :status) AND
        (:type IS NULL OR c.type = :type) AND
        (:city IS NULL OR LOWER(c.address.city) LIKE LOWER(CONCAT('%', :city, '%')))
    """)
    List<Customer> search(
        @Param("name") String name,
        @Param("email") String email,
        @Param("phone") String phone,
        @Param("status") CustomerStatus status,
        @Param("type") CustomerType type,
        @Param("city") String city
    );
}
