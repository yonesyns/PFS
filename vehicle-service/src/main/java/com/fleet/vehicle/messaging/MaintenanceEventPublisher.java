package com.fleet.vehicle.messaging;

import com.fleet.vehicle.entity.Maintenance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishMaintenanceScheduled(Maintenance maintenance, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MaintenanceScheduledEvent");
        event.put("maintenanceId", maintenance.getId());
        event.put("vehicleId", maintenance.getVehicleId());
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("maintenanceType", maintenance.getType().name());
        event.put("description", maintenance.getDescription());
        event.put("scheduledDate", maintenance.getScheduledDate().toString());
        event.put("estimatedCost", maintenance.getEstimatedCost());
        event.put("garageName", maintenance.getGarageName());
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.maintenance.scheduled", event);
        log.info("Published MaintenanceScheduledEvent for vehicle {}: {} on {}",
                plateNumber, maintenance.getType(), maintenance.getScheduledDate());
    }

    public void publishMaintenanceStarted(Maintenance maintenance, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MaintenanceStartedEvent");
        event.put("maintenanceId", maintenance.getId());
        event.put("vehicleId", maintenance.getVehicleId());
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("maintenanceType", maintenance.getType().name());
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.maintenance.started", event);
        log.info("Published MaintenanceStartedEvent for vehicle {}: {}",
                plateNumber, maintenance.getType());
    }

    public void publishMaintenanceCompleted(Maintenance maintenance, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MaintenanceCompletedEvent");
        event.put("maintenanceId", maintenance.getId());
        event.put("vehicleId", maintenance.getVehicleId());
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("maintenanceType", maintenance.getType().name());
        event.put("actualCost", maintenance.getActualCost());
        event.put("mileageAtMaintenance", maintenance.getMileageAtMaintenance());
        event.put("completedDate", maintenance.getCompletedDate() != null ? maintenance.getCompletedDate().toString() : null);
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.maintenance.completed", event);
        log.info("Published MaintenanceCompletedEvent for vehicle {}: {} - Cost: {} cents",
                plateNumber, maintenance.getType(), maintenance.getActualCost());
    }

    public void publishMaintenanceOverdue(Maintenance maintenance, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MaintenanceOverdueEvent");
        event.put("maintenanceId", maintenance.getId());
        event.put("vehicleId", maintenance.getVehicleId());
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("maintenanceType", maintenance.getType().name());
        event.put("description", maintenance.getDescription());
        event.put("scheduledDate", maintenance.getScheduledDate().toString());
        event.put("overdueDays", ChronoUnit.DAYS.between(
                maintenance.getScheduledDate(), LocalDate.now()));
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.maintenance.overdue", event);
        log.warn("Published MaintenanceOverdueEvent for vehicle {}: {} scheduled on {}",
                plateNumber, maintenance.getType(), maintenance.getScheduledDate());
    }

    public void publishMaintenanceCancelled(Maintenance maintenance, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "MaintenanceCancelledEvent");
        event.put("maintenanceId", maintenance.getId());
        event.put("vehicleId", maintenance.getVehicleId());
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("maintenanceType", maintenance.getType().name());
        event.put("cancellationReason", maintenance.getCancellationReason());
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.maintenance.cancelled", event);
        log.info("Published MaintenanceCancelledEvent for vehicle {}: {}",
                plateNumber, maintenance.getType());
    }

    public void publishVehicleBackToActive(UUID vehicleId, String plateNumber, UUID customerId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "VehicleStatusChangedEvent");
        event.put("vehicleId", vehicleId);
        event.put("plateNumber", plateNumber);
        event.put("customerId", customerId);
        event.put("newStatus", "ACTIVE");
        event.put("previousStatus", "MAINTENANCE");
        event.put("reason", "Maintenance completed");
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(exchange, "vehicle.status.changed", event);
        log.info("Published VehicleBackToActiveEvent for vehicle {}", plateNumber);
    }
}