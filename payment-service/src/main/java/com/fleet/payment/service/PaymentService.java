package com.fleet.payment.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceNotFoundException;
import com.fleet.payment.client.CustomerServiceClient;
import com.fleet.payment.client.VehicleServiceClient;
import com.fleet.payment.dto.*;
import com.fleet.payment.entity.*;
import com.fleet.payment.messaging.PaymentEventPublisher;
import com.fleet.payment.repository.InvoiceRepository;
import com.fleet.payment.repository.SubscriptionRepository;
import com.fleet.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentEventPublisher eventPublisher;
    private final CustomerServiceClient customerServiceClient;
    private final VehicleServiceClient vehicleServiceClient;

    private final AtomicInteger invoiceCounter = new AtomicInteger(0);

    // === INVOICE OPERATIONS ===

    public InvoiceResponse createInvoice(InvoiceRequest request) {
        log.info("Creating invoice for customer: {}", request.getCustomerId());

        validateCustomer(request.getCustomerId());
        if (request.getVehicleId() != null) {
            validateVehicle(request.getVehicleId());
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customerId(request.getCustomerId())
                .vehicleId(request.getVehicleId())
                .subscriptionId(request.getSubscriptionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {} with number: {}", saved.getId(), saved.getInvoiceNumber());

        eventPublisher.publishInvoiceCreated(saved);

        return toInvoiceResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        Invoice invoice = findInvoiceById(id);
        return toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getInvoicesByCustomer(UUID customerId, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByCustomerId(customerId, pageable);
        return buildInvoicePageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findAll(pageable);
        return buildInvoicePageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getOverdueInvoices(Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByStatus(InvoiceStatus.OVERDUE, pageable);
        return buildInvoicePageResponse(page);
    }

    public InvoiceResponse payInvoice(UUID id, PayInvoiceRequest request) {
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Invoice is cancelled");
        }

        // Simulate payment processing
        Transaction transaction = Transaction.builder()
                .invoiceId(invoice.getId())
                .amount(invoice.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.SUCCESS)
                .externalReference("SIM-" + UUID.randomUUID().toString().substring(0, 8))
                .transactionDate(java.time.Instant.now())
                .build();

        transactionRepository.save(transaction);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(java.time.Instant.now());
        Invoice updated = invoiceRepository.save(invoice);

        log.info("Invoice paid: {} via {}", id, request.getPaymentMethod());
        eventPublisher.publishInvoicePaid(updated);

        return toInvoiceResponse(updated);
    }

    public void cancelInvoice(UUID id, String reason) {
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);

        log.info("Invoice cancelled: {} - Reason: {}", id, reason);
        eventPublisher.publishInvoiceCancelled(invoice, reason);
    }

    // === SUBSCRIPTION OPERATIONS ===

    public SubscriptionResponse createSubscription(UUID customerId, PlanType planType, Long monthlyAmount) {
        log.info("Creating subscription for customer: {} with plan: {}", customerId, planType);

        Subscription subscription = Subscription.builder()
                .customerId(customerId)
                .planType(planType)
                .monthlyAmount(monthlyAmount)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(SubscriptionStatus.PENDING)
                .autoRenew(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Subscription created: {}", saved.getId());

        eventPublisher.publishSubscriptionCreated(saved);

        return toSubscriptionResponse(saved);
    }

    public SubscriptionResponse activateSubscription(UUID subscriptionId) {
        Subscription subscription = findSubscriptionById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Subscription updated = subscriptionRepository.save(subscription);

        log.info("Subscription activated: {}", subscriptionId);

        return toSubscriptionResponse(updated);
    }

    public SubscriptionResponse upgradeSubscription(UUID subscriptionId, PlanType newPlan, Long newAmount) {
        Subscription subscription = findSubscriptionById(subscriptionId);
        PlanType oldPlan = subscription.getPlanType();

        subscription.setPlanType(newPlan);
        subscription.setMonthlyAmount(newAmount);
        Subscription updated = subscriptionRepository.save(subscription);

        log.info("Subscription {} upgraded from {} to {}", subscriptionId, oldPlan, newPlan);
        eventPublisher.publishSubscriptionUpgraded(updated, oldPlan, newPlan);

        return toSubscriptionResponse(updated);
    }

    public void cancelSubscription(UUID subscriptionId, String reason) {
        Subscription subscription = findSubscriptionById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDate.now());
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled: {} - Reason: {}", subscriptionId, reason);
        eventPublisher.publishSubscriptionCancelled(subscription, reason);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(UUID id) {
        Subscription subscription = findSubscriptionById(id);
        return toSubscriptionResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toSubscriptionResponse)
                .collect(Collectors.toList());
    }

    // === TRANSACTION OPERATIONS ===

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactionsByInvoice(UUID invoiceId, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findByInvoiceId(invoiceId, pageable);
        return buildTransactionPageResponse(page);
    }

    // === SCHEDULED OPERATIONS ===

    public void generateMonthlyInvoices() {
        log.info("Generating monthly subscription invoices...");
        LocalDate today = LocalDate.now();

        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        for (Subscription sub : activeSubscriptions) {
            if (sub.getLastRenewalDate() == null || 
                    sub.getLastRenewalDate().plusMonths(1).isBefore(today)) {

                Invoice invoice = Invoice.builder()
                        .invoiceNumber(generateInvoiceNumber())
                        .customerId(sub.getCustomerId())
                        .subscriptionId(sub.getId())
                        .amount(sub.getMonthlyAmount())
                        .currency("EUR")
                        .description("Monthly subscription - " + sub.getPlanType())
                        .status(InvoiceStatus.SENT)
                        .issueDate(today)
                        .dueDate(today.plusDays(30))
                        .build();

                Invoice saved = invoiceRepository.save(invoice);
                sub.setLastRenewalDate(today);
                subscriptionRepository.save(sub);

                log.info("Generated monthly invoice: {} for subscription: {}", 
                        saved.getInvoiceNumber(), sub.getId());
                eventPublisher.publishInvoiceCreated(saved);
            }
        }
    }

    public void checkOverdueInvoices() {
        log.info("Checking overdue invoices...");
        LocalDate today = LocalDate.now();
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(today);

        for (Invoice invoice : overdue) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);

            int daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), today);
            log.info("Invoice {} is {} days overdue", invoice.getId(), daysOverdue);
            eventPublisher.publishInvoiceOverdue(invoice, daysOverdue);

            // If 60 days overdue, cancel subscription
            if (daysOverdue >= 60) {
                handleLongOverdueInvoice(invoice);
            }
        }
    }

    public void checkExpiringSubscriptions() {
        log.info("Checking expiring subscriptions...");
        LocalDate warningDate = LocalDate.now().plusDays(7);
        List<Subscription> expiring = subscriptionRepository.findExpiringSubscriptions(warningDate);

        for (Subscription sub : expiring) {
            if (!sub.getAutoRenew()) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                log.info("Subscription {} expired", sub.getId());
                eventPublisher.publishSubscriptionExpired(sub);
            }
        }
    }

    // === EVENT HANDLERS ===

    public void handleCustomerCreated(UUID customerId) {
        log.info("Handling customer creation: {} - Creating BASIC subscription", customerId);
        createSubscription(customerId, PlanType.BASIC, 0L);
    }

    public void handleCustomerValidated(UUID customerId) {
        log.info("Handling customer validation: {} - Activating subscription", customerId);
        subscriptionRepository.findByCustomerIdAndStatus(customerId, SubscriptionStatus.PENDING)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(sub);
                });
    }

    public void handleCustomerDeleted(UUID customerId) {
        log.info("Handling customer deletion: {} - Cancelling subscriptions", customerId);
        subscriptionRepository.findByCustomerIdAndStatus(customerId, SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> cancelSubscription(sub.getId(), "Customer deleted"));
    }

    public void handleVehicleAssigned(UUID vehicleId, UUID customerId) {
        log.info("Handling vehicle assignment: {} to customer: {} - Creating activation invoice", 
                vehicleId, customerId);

        InvoiceRequest request = InvoiceRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .amount(5000L) // 50 EUR in cents
                .currency("EUR")
                .description("Vehicle activation fee")
                .build();

        createInvoice(request);
    }

    // === PRIVATE METHODS ===

    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        int count = invoiceCounter.incrementAndGet();
        return prefix + String.format("%05d", count);
    }

    private void validateCustomer(UUID customerId) {
        Boolean exists = customerServiceClient.customerExists(customerId).block();
        if (Boolean.FALSE.equals(exists)) {
            throw new BadRequestException("Customer does not exist: " + customerId);
        }
    }

    private void validateVehicle(UUID vehicleId) {
        Boolean exists = vehicleServiceClient.vehicleExists(vehicleId).block();
        if (Boolean.FALSE.equals(exists)) {
            throw new BadRequestException("Vehicle does not exist: " + vehicleId);
        }
    }

    private void handleLongOverdueInvoice(Invoice invoice) {
        subscriptionRepository.findByCustomerIdAndStatus(invoice.getCustomerId(), SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    cancelSubscription(sub.getId(), "Invoice overdue for 60+ days");
                    eventPublisher.publishSubscriptionExpired(sub);
                });
    }

    private Invoice findInvoiceById(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    private Subscription findSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomerId())
                .vehicleId(invoice.getVehicleId())
                .subscriptionId(invoice.getSubscriptionId())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .description(invoice.getDescription())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomerId())
                .planType(subscription.getPlanType())
                .monthlyAmount(subscription.getMonthlyAmount())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .autoRenew(subscription.getAutoRenew())
                .lastRenewalDate(subscription.getLastRenewalDate())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .invoiceId(transaction.getInvoiceId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .externalReference(transaction.getExternalReference())
                .transactionDate(transaction.getTransactionDate())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private PageResponse<InvoiceResponse> buildInvoicePageResponse(Page<Invoice> page) {
        return PageResponse.<InvoiceResponse>builder()
                .content(page.getContent().stream().map(this::toInvoiceResponse).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private PageResponse<TransactionResponse> buildTransactionPageResponse(Page<Transaction> page) {
        return PageResponse.<TransactionResponse>builder()
                .content(page.getContent().stream().map(this::toTransactionResponse).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
