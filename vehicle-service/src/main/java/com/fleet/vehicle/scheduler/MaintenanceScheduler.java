package com.fleet.vehicle.scheduler;

import com.fleet.vehicle.service.MaintenanceService;
import com.fleet.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final VehicleService vehicleService;
    private final MaintenanceService maintenanceService;

    // === VÉHICULE EXISTANT ===

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredInsurance() {
        log.info("Running scheduled task: checkExpiredInsurance");
        vehicleService.checkExpiredInsurance();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void checkMaintenanceDue() {
        log.info("Running scheduled task: checkMaintenanceDue (technical inspection)");
        vehicleService.checkMaintenanceDue();
    }

    // === NOUVEAU : MAINTENANCE ===

    @Scheduled(cron = "0 0 4 * * ?")
    public void checkOverdueMaintenances() {
        log.info("Running scheduled task: checkOverdueMaintenances");
        maintenanceService.checkOverdueMaintenances();
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void checkPreventivePlansDue() {
        log.info("Running scheduled task: checkPreventivePlansDue");
        maintenanceService.checkPreventivePlansDue();
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void generateMaintenanceAlerts() {
        log.info("Running scheduled task: generateMaintenanceAlerts");
        maintenanceService.generateMaintenanceAlerts();
    }
}