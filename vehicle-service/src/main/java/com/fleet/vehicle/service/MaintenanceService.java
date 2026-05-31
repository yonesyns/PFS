package com.fleet.vehicle.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceNotFoundException;
import com.fleet.vehicle.dto.*;
import com.fleet.vehicle.entity.*;
import com.fleet.vehicle.messaging.MaintenanceEventPublisher;
import com.fleet.vehicle.repository.MaintenancePlanRepository;
import com.fleet.vehicle.repository.MaintenanceRepository;
import com.fleet.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final MaintenanceEventPublisher eventPublisher;

    // === MAINTENANCE CRUD ===

    public MaintenanceResponse scheduleMaintenance(UUID vehicleId, MaintenanceRequest request) {
        log.info("Scheduling maintenance for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);

        // Véhicule ne peut pas être en maintenance si SOLD ou ORPHANED
        if (vehicle.getStatus() == VehicleStatus.SOLD) {
            throw new BadRequestException("Cannot schedule maintenance for a sold vehicle");
        }

        Maintenance maintenance = Maintenance.builder()
                .vehicleId(vehicleId)
                .type(request.getType())
                .description(request.getDescription())
                .scheduledDate(request.getScheduledDate())
                .estimatedCost(request.getEstimatedCost())
                .garageName(request.getGarageName())
                .garageContact(request.getGarageContact())
                .partsUsed(request.getPartsUsed())
                .technicianName(request.getTechnicianName())
                .notes(request.getNotes())
                .status(MaintenanceStatus.SCHEDULED)
                .build();

        Maintenance saved = maintenanceRepository.save(maintenance);
        log.info("Maintenance scheduled: {} for vehicle: {}", saved.getId(), vehicleId);

        eventPublisher.publishMaintenanceScheduled(saved, vehicle.getPlateNumber(), vehicle.getCustomerId());

        return toResponse(saved, vehicle.getPlateNumber());
    }

    public MaintenanceResponse startMaintenance(UUID maintenanceId) {
        log.info("Starting maintenance: {}", maintenanceId);

        Maintenance maintenance = findMaintenanceById(maintenanceId);
        Vehicle vehicle = vehicleService.findVehicleById(maintenance.getVehicleId());

        if (maintenance.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new BadRequestException("Maintenance must be SCHEDULED to start");
        }

        if (vehicle.getStatus() == VehicleStatus.SOLD) {
            throw new BadRequestException("Cannot start maintenance on a sold vehicle");
        }

        maintenance.setStatus(MaintenanceStatus.IN_PROGRESS);
        Maintenance saved = maintenanceRepository.save(maintenance);

        // Mettre le véhicule en MAINTENANCE
        if (vehicle.getStatus() != VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle {} status changed to MAINTENANCE", vehicle.getId());
        }

        eventPublisher.publishMaintenanceStarted(saved, vehicle.getPlateNumber(), vehicle.getCustomerId());

        return toResponse(saved, vehicle.getPlateNumber());
    }

    public MaintenanceResponse completeMaintenance(UUID maintenanceId, MaintenanceCompletionRequest request) {
        log.info("Completing maintenance: {}", maintenanceId);

        Maintenance maintenance = findMaintenanceById(maintenanceId);
        Vehicle vehicle = vehicleService.findVehicleById(maintenance.getVehicleId());

        if (maintenance.getStatus() != MaintenanceStatus.IN_PROGRESS) {
            throw new BadRequestException("Maintenance must be IN_PROGRESS to complete");
        }

        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setCompletedDate(request.getCompletedDate());
        maintenance.setActualCost(request.getActualCost());
        maintenance.setMileageAtMaintenance(request.getMileageAtMaintenance());
        maintenance.setPartsUsed(request.getPartsUsed());
        maintenance.setNotes(request.getNotes());

        Maintenance saved = maintenanceRepository.save(maintenance);

        // Mettre à jour le kilométrage du véhicule si fourni
        if (request.getMileageAtMaintenance() != null) {
            vehicle.setMileage(request.getMileageAtMaintenance());
        }

        // Remettre le véhicule en ACTIVE (si tout est OK)
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            // Vérifier que le CT n'est pas dépassé
            if (vehicle.getTechnicalInspectionDate() == null ||
                    vehicle.getTechnicalInspectionDate().plusMonths(6).isBefore(LocalDate.now())) {
                vehicle.setStatus(VehicleStatus.MAINTENANCE); // Reste en MAINTENANCE
                log.warn("Vehicle {} remains MAINTENANCE - technical inspection overdue", vehicle.getId());
            } else if (vehicle.getInsuranceExpiryDate() != null &&
                    vehicle.getInsuranceExpiryDate().isBefore(LocalDate.now())) {
                vehicle.setStatus(VehicleStatus.INACTIVE); // Assurance expirée
                log.warn("Vehicle {} set to INACTIVE - insurance expired", vehicle.getId());
            } else {
                vehicle.setStatus(VehicleStatus.ACTIVE);
                log.info("Vehicle {} back to ACTIVE after maintenance", vehicle.getId());
            }
        }

        vehicleRepository.save(vehicle);

        // Mettre à jour le plan de maintenance préventif si applicable
        updateMaintenancePlan(vehicle.getId(), request.getCompletedDate(), request.getMileageAtMaintenance());

        eventPublisher.publishMaintenanceCompleted(saved, vehicle.getPlateNumber(), vehicle.getCustomerId());
        // ET AJOUTER APRÈS vehicleRepository.save(vehicle);
        if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
            eventPublisher.publishVehicleBackToActive(vehicle.getId(), vehicle.getPlateNumber(), vehicle.getCustomerId());
        }

        return toResponse(saved, vehicle.getPlateNumber());
    }

    public MaintenanceResponse cancelMaintenance(UUID maintenanceId, String reason) {
        log.info("Cancelling maintenance: {} - Reason: {}", maintenanceId, reason);

        Maintenance maintenance = findMaintenanceById(maintenanceId);

        if (maintenance.getStatus() == MaintenanceStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed maintenance");
        }

        maintenance.setStatus(MaintenanceStatus.CANCELLED);
        maintenance.setCancellationReason(reason);
        Maintenance saved = maintenanceRepository.save(maintenance);

        // Si le véhicule était en MAINTENANCE à cause de cette intervention, le remettre en ACTIVE
        Vehicle vehicle = vehicleService.findVehicleById(maintenance.getVehicleId());
        List<Maintenance> activeMaintenances = maintenanceRepository.findActiveByVehicleId(vehicle.getId());
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE && activeMaintenances.isEmpty()) {
            vehicle.setStatus(VehicleStatus.ACTIVE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle {} back to ACTIVE - no active maintenances", vehicle.getId());
        }
        eventPublisher.publishMaintenanceCancelled(saved, vehicle.getPlateNumber(), vehicle.getCustomerId());

        return toResponse(saved, vehicle.getPlateNumber());
    }

    // === QUERIES ===

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getMaintenanceHistory(UUID vehicleId) {
        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);
        return maintenanceRepository.findByVehicleIdOrderByScheduledDateDesc(vehicleId)
                .stream()
                .map(m -> toResponse(m, vehicle.getPlateNumber()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse> getMaintenancesByStatus(MaintenanceStatus status, Pageable pageable) {
        Page<Maintenance> page = maintenanceRepository.findByStatus(status, pageable);
        List<MaintenanceResponse> content = page.getContent().stream()
                .map(m -> {
                    Vehicle v = vehicleRepository.findById(m.getVehicleId()).orElse(null);
                    return toResponse(m, v != null ? v.getPlateNumber() : "UNKNOWN");
                })
                .collect(Collectors.toList());

        return PageResponse.<MaintenanceResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceAlertResponse> getUpcomingMaintenances(int daysAhead) {
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(daysAhead);

        return maintenanceRepository.findUpcoming(now, end).stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceAlertResponse> getOverdueMaintenances() {
        return maintenanceRepository.findOverdue(LocalDate.now()).stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
    }

    // === MAINTENANCE PLANS ===

    public MaintenancePlan createMaintenancePlan(UUID vehicleId, MaintenancePlanRequest request) {
        log.info("Creating maintenance plan for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleService.findVehicleById(vehicleId);

        MaintenancePlan plan = MaintenancePlan.builder()
                .vehicleId(vehicleId)
                .name(request.getName())
                .intervalType(request.getIntervalType())
                .intervalValue(request.getIntervalValue())
                .alertDaysBefore(request.getAlertDaysBefore())
                .isActive(true)
                .build();

        // Calculer la prochaine échéance
        calculateNextDue(plan, vehicle.getMileage());

        MaintenancePlan saved = maintenancePlanRepository.save(plan);
        log.info("Maintenance plan created: {} for vehicle: {}", saved.getId(), vehicleId);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<MaintenancePlan> getMaintenancePlans(UUID vehicleId) {
        return maintenancePlanRepository.findByVehicleIdAndIsActiveTrue(vehicleId);
    }

    public void deactivateMaintenancePlan(UUID planId) {
        MaintenancePlan plan = maintenancePlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenancePlan", "id", planId));
        plan.setIsActive(false);
        maintenancePlanRepository.save(plan);
        log.info("Maintenance plan deactivated: {}", planId);
    }

    // === SCHEDULED TASKS ===

    public void checkOverdueMaintenances() {
        log.info("Checking overdue maintenances...");
        LocalDate today = LocalDate.now();

        List<Maintenance> overdue = maintenanceRepository.findOverdue(today);
        for (Maintenance m : overdue) {
            m.setStatus(MaintenanceStatus.OVERDUE);
            maintenanceRepository.save(m);

            Vehicle vehicle = vehicleService.findVehicleById(m.getVehicleId());
            eventPublisher.publishMaintenanceOverdue(m, vehicle.getPlateNumber(), vehicle.getCustomerId());

            log.warn("Maintenance {} is overdue for vehicle {}", m.getId(), m.getVehicleId());
        }
    }

    public void checkPreventivePlansDue() {
        log.info("Checking preventive maintenance plans...");
        LocalDate alertDate = LocalDate.now().plusDays(30);

        List<MaintenancePlan> plansDue = maintenancePlanRepository.findPlansDueByDate(alertDate);
        for (MaintenancePlan plan : plansDue) {
            Vehicle vehicle = vehicleService.findVehicleById(plan.getVehicleId());

            if (plan.isDueBasedOnDate()) {
                log.warn("Maintenance plan '{}' due for vehicle {} (date: {})",
                        plan.getName(), vehicle.getPlateNumber(), plan.getNextDueDate());
                // TODO: Envoyer notification au CUSTOMER_ADMIN
            }

            if (plan.isDueBasedOnMileage(vehicle.getMileage())) {
                log.warn("Maintenance plan '{}' due for vehicle {} (mileage: {} / {})",
                        plan.getName(), vehicle.getPlateNumber(), vehicle.getMileage(), plan.getNextDueMileage());
                // TODO: Envoyer notification au CUSTOMER_ADMIN
            }
        }
    }

    public void generateMaintenanceAlerts() {
        log.info("Generating maintenance alerts...");
        LocalDate now = LocalDate.now();

        // Alertes 30 jours avant
        List<Maintenance> upcoming30 = maintenanceRepository.findUpcoming(
                now.plusDays(29), now.plusDays(30));
        for (Maintenance m : upcoming30) {
            Vehicle v = vehicleService.findVehicleById(m.getVehicleId());
            log.info("ALERT [30 days]: Maintenance {} for vehicle {} on {}",
                    m.getType(), v.getPlateNumber(), m.getScheduledDate());
        }

        // Alertes 15 jours avant
        List<Maintenance> upcoming15 = maintenanceRepository.findUpcoming(
                now.plusDays(14), now.plusDays(15));
        for (Maintenance m : upcoming15) {
            Vehicle v = vehicleService.findVehicleById(m.getVehicleId());
            log.warn("ALERT [15 days]: Maintenance {} for vehicle {} on {}",
                    m.getType(), v.getPlateNumber(), m.getScheduledDate());
        }

        // Alertes 7 jours avant
        List<Maintenance> upcoming7 = maintenanceRepository.findUpcoming(
                now.plusDays(6), now.plusDays(7));
        for (Maintenance m : upcoming7) {
            Vehicle v = vehicleService.findVehicleById(m.getVehicleId());
            log.error("ALERT [7 days URGENT]: Maintenance {} for vehicle {} on {}",
                    m.getType(), v.getPlateNumber(), m.getScheduledDate());
        }
    }

    // === PRIVATE METHODS ===

    private Maintenance findMaintenanceById(UUID id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", "id", id));
    }

    private void updateMaintenancePlan(UUID vehicleId, LocalDate completedDate, Long mileage) {
        List<MaintenancePlan> plans = maintenancePlanRepository.findByVehicleIdAndIsActiveTrue(vehicleId);
        for (MaintenancePlan plan : plans) {
            plan.setLastDoneDate(completedDate);
            plan.setLastDoneMileage(mileage);
            calculateNextDue(plan, mileage);
            maintenancePlanRepository.save(plan);
            log.info("Updated maintenance plan: {} for vehicle: {}", plan.getId(), vehicleId);
        }
    }

    private void calculateNextDue(MaintenancePlan plan, Long currentMileage) {
        if (plan.getIntervalType() == MaintenancePlan.IntervalType.TIME_BASED) {
            plan.setNextDueDate(LocalDate.now().plusMonths(plan.getIntervalValue()));
        } else if (plan.getIntervalType() == MaintenancePlan.IntervalType.KM_BASED && currentMileage != null) {
            plan.setNextDueMileage(currentMileage + plan.getIntervalValue());
        }
    }

    private MaintenanceResponse toResponse(Maintenance maintenance, String plateNumber) {
        return MaintenanceResponse.builder()
                .id(maintenance.getId())
                .vehicleId(maintenance.getVehicleId())
                .vehiclePlateNumber(plateNumber)
                .type(maintenance.getType())
                .description(maintenance.getDescription())
                .scheduledDate(maintenance.getScheduledDate())
                .completedDate(maintenance.getCompletedDate())
                .status(maintenance.getStatus())
                .estimatedCost(maintenance.getEstimatedCost())
                .actualCost(maintenance.getActualCost())
                .mileageAtMaintenance(maintenance.getMileageAtMaintenance())
                .garageName(maintenance.getGarageName())
                .garageContact(maintenance.getGarageContact())
                .partsUsed(maintenance.getPartsUsed())
                .technicianName(maintenance.getTechnicianName())
                .notes(maintenance.getNotes())
                .cancellationReason(maintenance.getCancellationReason())
                .overdue(maintenance.isOverdue())
                .createdAt(maintenance.getCreatedAt())
                .updatedAt(maintenance.getUpdatedAt())
                .build();
    }

    private MaintenanceAlertResponse toAlertResponse(Maintenance maintenance) {
        Vehicle vehicle = vehicleService.findVehicleById(maintenance.getVehicleId());
        LocalDate now = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(now, maintenance.getScheduledDate());

        String alertLevel;
        String message;

        if (daysDiff < 0) {
            alertLevel = "CRITICAL";
            message = String.format("Maintenance OVERDUE by %d days for vehicle %s",
                    Math.abs(daysDiff), vehicle.getPlateNumber());
        } else if (daysDiff <= 7) {
            alertLevel = "CRITICAL";
            message = String.format("Maintenance due in %d days for vehicle %s",
                    daysDiff, vehicle.getPlateNumber());
        } else if (daysDiff <= 15) {
            alertLevel = "WARNING";
            message = String.format("Maintenance due in %d days for vehicle %s",
                    daysDiff, vehicle.getPlateNumber());
        } else {
            alertLevel = "INFO";
            message = String.format("Maintenance scheduled in %d days for vehicle %s",
                    daysDiff, vehicle.getPlateNumber());
        }

        return MaintenanceAlertResponse.builder()
                .maintenanceId(maintenance.getId())
                .vehicleId(maintenance.getVehicleId())
                .vehiclePlateNumber(vehicle.getPlateNumber())
                .type(maintenance.getType())
                .description(maintenance.getDescription())
                .scheduledDate(maintenance.getScheduledDate())
                .daysUntilDue((int) Math.max(0, daysDiff))
                .overdueDays(daysDiff < 0 ? Math.abs(daysDiff) : null)
                .alertLevel(alertLevel)
                .message(message)
                .build();
    }
}