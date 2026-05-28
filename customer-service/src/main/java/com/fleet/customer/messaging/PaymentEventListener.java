package com.fleet.customer.messaging;

import com.fleet.commons.event.payment.InvoiceOverdueEvent;
import com.fleet.commons.event.payment.InvoicePaidEvent;
import com.fleet.commons.event.payment.SubscriptionExpiredEvent;
import com.fleet.customer.entity.Customer;
import com.fleet.customer.entity.CustomerStatus;
import com.fleet.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final CustomerRepository customerRepository;

    @RabbitListener(queues = "payment.events")
    public void handleInvoicePaid(InvoicePaidEvent event) {
        log.info("Received InvoicePaidEvent for customer: {}", event.getPayload().getCustomerId());

        customerRepository.findById(event.getPayload().getCustomerId())
                .ifPresent(customer -> {
                    if (customer.getStatus() == CustomerStatus.SUSPENDED) {
                        customer.setStatus(CustomerStatus.ACTIVE);
                        customerRepository.save(customer);
                        log.info("Customer {} reactivated after invoice payment", customer.getId());
                    }
                });
    }

    @RabbitListener(queues = "payment.events")
    public void handleInvoiceOverdue(InvoiceOverdueEvent event) {
        log.info("Received InvoiceOverdueEvent for customer: {}", event.getPayload().getCustomerId());

        customerRepository.findById(event.getPayload().getCustomerId())
                .ifPresent(customer -> {
                    customer.setStatus(CustomerStatus.SUSPENDED);
                    customerRepository.save(customer);
                    log.info("Customer {} suspended due to overdue invoice", customer.getId());
                });
    }

    @RabbitListener(queues = "payment.events")
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info("Received SubscriptionExpiredEvent for customer: {}", event.getPayload().getCustomerId());

        customerRepository.findById(event.getPayload().getCustomerId())
                .ifPresent(customer -> {
                    customer.setStatus(CustomerStatus.INACTIVE);
                    customerRepository.save(customer);
                    log.info("Customer {} set to INACTIVE due to expired subscription", customer.getId());
                });
    }
}
