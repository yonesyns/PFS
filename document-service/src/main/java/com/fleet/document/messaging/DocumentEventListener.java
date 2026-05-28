package com.fleet.document.messaging;

import com.fleet.commons.event.customer.CustomerCreatedEvent;
import com.fleet.commons.event.payment.InvoiceCreatedEvent;
import com.fleet.commons.event.payment.InvoicePaidEvent;
import com.fleet.commons.event.vehicle.VehicleCreatedEvent;
import com.fleet.commons.event.vehicle.VehicleStatusChangedEvent;
import com.fleet.document.entity.DocumentType;
import com.fleet.document.entity.EntityType;
import com.fleet.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventListener {

    private final DocumentService documentService;

    @RabbitListener(queues = "customer.events")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent for customer: {}", event.getPayload().getCustomerId());
        documentService.createDocumentFolder(
                EntityType.CUSTOMER, 
                event.getPayload().getCustomerId().toString());
    }

    @RabbitListener(queues = "vehicle.events")
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        log.info("Received VehicleCreatedEvent for vehicle: {}", event.getPayload().getVehicleId());
        documentService.createDocumentFolder(
                EntityType.VEHICLE, 
                event.getPayload().getVehicleId().toString());
    }

    @RabbitListener(queues = "payment.events")
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("Received InvoiceCreatedEvent for invoice: {}", event.getPayload().getInvoiceId());
        // Document service will generate and store the PDF invoice
        // This is handled by the payment service triggering document generation
    }

    @RabbitListener(queues = "payment.events")
    public void handleInvoicePaid(InvoicePaidEvent event) {
        log.info("Received InvoicePaidEvent for invoice: {}", event.getPayload().getInvoiceId());
        documentService.markInvoiceAsPaid(event.getPayload().getInvoiceId().toString());
    }

    @RabbitListener(queues = "vehicle.events")
    public void handleVehicleStatusChanged(VehicleStatusChangedEvent event) {
        log.info("Received VehicleStatusChangedEvent for vehicle: {} to {}", 
                event.getPayload().getVehicleId(), event.getPayload().getNewStatus());

        if ("MAINTENANCE".equals(event.getPayload().getNewStatus())) {
            // Create maintenance report template
            log.info("Creating maintenance report template for vehicle: {}", 
                    event.getPayload().getVehicleId());
        }
    }
}
