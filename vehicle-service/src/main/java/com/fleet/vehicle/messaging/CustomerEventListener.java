package com.fleet.vehicle.messaging;

import com.fleet.commons.event.customer.CustomerDeletedEvent;
import com.fleet.commons.event.customer.CustomerReactivatedEvent;
import com.fleet.commons.event.customer.CustomerSuspendedEvent;
import com.fleet.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final VehicleService vehicleService;

    @RabbitListener(queues = "customer.events")
    public void handleCustomerDeleted(CustomerDeletedEvent event) {
        log.info("Received CustomerDeletedEvent for customer: {}", event.getPayload().getCustomerId());
        vehicleService.handleCustomerDeleted(event.getPayload().getCustomerId());
    }

    @RabbitListener(queues = "customer.events")
    public void handleCustomerSuspended(CustomerSuspendedEvent event) {
        log.info("Received CustomerSuspendedEvent for customer: {}", event.getPayload().getCustomerId());
        vehicleService.handleCustomerSuspended(event.getPayload().getCustomerId());
    }

    @RabbitListener(queues = "customer.events")
    public void handleCustomerReactivated(CustomerReactivatedEvent event) {
        log.info("Received CustomerReactivatedEvent for customer: {}", event.getPayload().getCustomerId());
        vehicleService.handleCustomerReactivated(event.getPayload().getCustomerId());
    }
}
