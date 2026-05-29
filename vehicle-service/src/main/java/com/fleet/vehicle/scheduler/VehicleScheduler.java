package com.fleet.vehicle.scheduler;

import com.fleet.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleScheduler {

    private final VehicleService vehicleService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiredInsurance() {
        log.info("Running scheduled task: checkExpiredInsurance");
        vehicleService.checkExpiredInsurance();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void checkMaintenanceDue() {
        log.info("Running scheduled task: checkMaintenanceDue");
        vehicleService.checkMaintenanceDue();
    }
}
