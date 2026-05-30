package com.fleet.vehicle.repository;

import com.fleet.vehicle.entity.Maintenance;
import com.fleet.vehicle.entity.MaintenanceStatus;
import com.fleet.vehicle.entity.MaintenanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {

    List<Maintenance> findByVehicleIdOrderByScheduledDateDesc(UUID vehicleId);

    Page<Maintenance> findByVehicleId(UUID vehicleId, Pageable pageable);

    Page<Maintenance> findByStatus(MaintenanceStatus status, Pageable pageable);

    List<Maintenance> findByStatusAndScheduledDateBefore(MaintenanceStatus status, LocalDate date);

    @Query("SELECT m FROM Maintenance m WHERE m.vehicleId = :vehicleId AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<Maintenance> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT m FROM Maintenance m WHERE m.status = 'SCHEDULED' AND m.scheduledDate BETWEEN :start AND :end")
    List<Maintenance> findUpcoming(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT m FROM Maintenance m WHERE m.status = 'SCHEDULED' AND m.scheduledDate < :today")
    List<Maintenance> findOverdue(@Param("today") LocalDate today);

    long countByVehicleIdAndStatus(UUID vehicleId, MaintenanceStatus status);

    @Query("SELECT COUNT(m) FROM Maintenance m WHERE m.status = :status AND m.type = :type")
    long countByStatusAndType(@Param("status") MaintenanceStatus status, @Param("type") MaintenanceType type);
}