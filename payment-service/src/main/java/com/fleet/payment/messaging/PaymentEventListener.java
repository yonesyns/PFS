package com.fleet.payment.messaging;

import com.fleet.commons.event.customer.CustomerCreatedEvent;
import com.fleet.commons.event.customer.CustomerDeletedEvent;
import com.fleet.commons.event.customer.CustomerValidatedEvent;
import com.fleet.commons.event.vehicle.VehicleAssignedEvent;
import com.fleet.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "customer.events")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent for customer: {}", event.getPayload().getCustomerId());
        paymentService.handleCustomerCreated(event.getPayload().getCustomerId());
    }

    @RabbitListener(queues = "customer.events")
    public void handleCustomerValidated(CustomerValidatedEvent event) {
        log.info("Received CustomerValidatedEvent for customer: {}", event.getPayload().getCustomerId());
        paymentService.handleCustomerValidated(event.getPayload().getCustomerId());
    }

    @RabbitListener(queues = "customer.events")
    public void handleCustomerDeleted(CustomerDeletedEvent event) {
        log.info("Received CustomerDeletedEvent for customer: {}", event.getPayload().getCustomerId());
        paymentService.handleCustomerDeleted(event.getPayload().getCustomerId());
    }

    @RabbitListener(queues = "vehicle.events")
    public void handleVehicleAssigned(VehicleAssignedEvent event) {
        log.info("Received VehicleAssignedEvent for vehicle: {} to customer: {}", 
                event.getPayload().getVehicleId(), event.getPayload().getNewCustomerId());

        if (event.getPayload().getNewCustomerId() != null) {
            paymentService.handleVehicleAssigned(
                    event.getPayload().getVehicleId(), 
                    event.getPayload().getNewCustomerId());
        }
    }
}
