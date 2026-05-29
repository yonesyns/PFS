package com.fleet.vehicle.repository;

import com.fleet.vehicle.entity.Vehicle;
import com.fleet.vehicle.entity.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    Optional<Vehicle> findByVin(String vin);

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByVin(String vin);

    Page<Vehicle> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.insuranceExpiryDate <= :date AND v.status != 'SOLD'")
    Page<Vehicle> findExpiringInsurance(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.technicalInspectionDate <= :date AND v.status != 'SOLD'")
    List<Vehicle> findMaintenanceDue(@Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE Vehicle v SET v.status = :status WHERE v.customerId = :customerId")
    int updateStatusByCustomerId(@Param("customerId") UUID customerId, @Param("status") VehicleStatus status);

    @Modifying
    @Query("UPDATE Vehicle v SET v.customerId = NULL, v.status = 'ORPHANED' WHERE v.customerId = :customerId")
    int orphanVehiclesByCustomerId(@Param("customerId") UUID customerId);

    long countByStatus(VehicleStatus status);

    long countByCustomerId(UUID customerId);
}
