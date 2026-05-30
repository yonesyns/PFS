package com.fleet.vehicle.messaging;

import com.fleet.commons.event.vehicle.*;
import com.fleet.vehicle.entity.Vehicle;
import com.fleet.vehicle.entity.VehicleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishVehicleCreated(Vehicle vehicle) {
        VehicleCreatedEvent event = VehicleCreatedEvent.builder()
                .eventType("VehicleCreatedEvent")
                .source("vehicle-service")
                .payload(VehicleCreatedEvent.VehiclePayload.builder()
                        .vehicleId(vehicle.getId())
                        .plateNumber(vehicle.getPlateNumber())
                        .brand(vehicle.getBrand())
                        .model(vehicle.getModel())
                        .customerId(vehicle.getCustomerId())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "vehicle.created", event);
        log.info("Published VehicleCreatedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleAssigned(Vehicle vehicle, UUID oldCustomerId, UUID newCustomerId) {
        VehicleAssignedEvent event = VehicleAssignedEvent.builder()
                .eventType("VehicleAssignedEvent")
                .source("vehicle-service")
                .payload(VehicleAssignedEvent.VehicleAssignedPayload.builder()
                        .vehicleId(vehicle.getId())
                        .oldCustomerId(oldCustomerId)
                        .newCustomerId(newCustomerId)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "vehicle.assigned", event);
        log.info("Published VehicleAssignedEvent for vehicle: {} to customer: {}", vehicle.getId(), newCustomerId);
    }

    public void publishVehicleUnassigned(Vehicle vehicle, UUID oldCustomerId) {
        VehicleUnassignedEvent event = VehicleUnassignedEvent.builder()
                .eventType("VehicleUnassignedEvent")
                .source("vehicle-service")
                .payload(VehicleUnassignedEvent.VehicleUnassignedPayload.builder()
                        .vehicleId(vehicle.getId())
                        .oldCustomerId(oldCustomerId)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "vehicle.unassigned", event);
        log.info("Published VehicleUnassignedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleStatusChanged(Vehicle vehicle, VehicleStatus oldStatus, VehicleStatus newStatus, String reason) {
        VehicleStatusChangedEvent event = VehicleStatusChangedEvent.builder()
                .eventType("VehicleStatusChangedEvent")
                .source("vehicle-service")
                .payload(VehicleStatusChangedEvent.VehicleStatusPayload.builder()
                        .vehicleId(vehicle.getId())
                        .oldStatus(oldStatus.name())
                        .newStatus(newStatus.name())
                        .reason(reason)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "vehicle.status.changed", event);
        log.info("Published VehicleStatusChangedEvent for vehicle: {} from {} to {}", 
                vehicle.getId(), oldStatus, newStatus);
    }

    public void publishVehicleDeleted(Vehicle vehicle) {
        VehicleDeletedEvent event = VehicleDeletedEvent.builder()
                .eventType("VehicleDeletedEvent")
                .source("vehicle-service")
                .payload(VehicleDeletedEvent.VehicleDeletedPayload.builder()
                        .vehicleId(vehicle.getId())
                        .customerId(vehicle.getCustomerId())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "vehicle.deleted", event);
        log.info("Published VehicleDeletedEvent for vehicle: {}", vehicle.getId());
    }
}
