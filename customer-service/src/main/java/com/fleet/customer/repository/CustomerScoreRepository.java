package com.fleet.customer.repository;

import com.fleet.customer.model.CustomerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerScoreRepository extends JpaRepository<CustomerScore, Long> {
    Optional<CustomerScore> findByCustomerId(Long customerId);
}
