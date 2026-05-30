package com.fleet.vehicle.messaging;

import com.fleet.commons.event.vehicle.VehicleStatusChangedEvent;
import com.fleet.vehicle.entity.Maintenance;
import com.fleet.vehicle.entity.VehicleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishMaintenanceScheduled(Maintenance maintenance, String plateNumber) {
        // Tu peux créer un MaintenanceScheduledEvent dans fleet-commons si besoin
        log.info("Maintenance scheduled for vehicle {}: {} on {}",
                plateNumber, maintenance.getType(), maintenance.getScheduledDate());
    }

    public void publishMaintenanceStarted(Maintenance maintenance, String plateNumber) {
        log.info("Maintenance started for vehicle {}: {}",
                plateNumber, maintenance.getType());
    }

    public void publishMaintenanceCompleted(Maintenance maintenance, String plateNumber) {
        log.info("Maintenance completed for vehicle {}: {} - Cost: {} cents",
                plateNumber, maintenance.getType(), maintenance.getActualCost());
    }

    public void publishMaintenanceOverdue(Maintenance maintenance, String plateNumber) {
        log.warn("Maintenance OVERDUE for vehicle {}: {} scheduled on {}",
                plateNumber, maintenance.getType(), maintenance.getScheduledDate());
    }
}