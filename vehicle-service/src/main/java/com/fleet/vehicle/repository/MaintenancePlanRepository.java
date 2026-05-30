package com.fleet.vehicle.repository;

import com.fleet.vehicle.entity.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {

    List<MaintenancePlan> findByVehicleIdAndIsActiveTrue(UUID vehicleId);

    List<MaintenancePlan> findByIsActiveTrue();

    @Query("SELECT mp FROM MaintenancePlan mp WHERE mp.isActive = true AND mp.nextDueDate <= :date")
    List<MaintenancePlan> findPlansDueByDate(@Param("date") java.time.LocalDate date);

    Optional<MaintenancePlan> findByVehicleIdAndName(UUID vehicleId, String name);
}