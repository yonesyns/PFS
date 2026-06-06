package com.fleet.customer.service;

import com.fleet.commons.dto.PageResponse;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceConflictException;
import com.fleet.commons.exception.ResourceNotFoundException;
import com.fleet.customer.dto.*;
import com.fleet.customer.entity.Customer;
import com.fleet.customer.entity.CustomerStatus;
import com.fleet.customer.mapper.CustomerMapper;
import com.fleet.customer.messaging.CustomerEventPublisher;
import com.fleet.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerEventPublisher eventPublisher;

    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer: {}", request.getCompanyName());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email already in use: " + request.getEmail());
        }
        if (customerRepository.existsByVatNumber(request.getVatNumber())) {
            throw new ResourceConflictException("VAT number already in use: " + request.getVatNumber());
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setStatus(CustomerStatus.PENDING);

        Customer saved = customerRepository.save(customer);
        log.info("Customer created with id: {}", saved.getId());

        eventPublisher.publishCustomerCreated(saved);

        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID id) {
        Customer customer = findCustomerById(id);
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> searchCustomers(CustomerSearchRequest searchRequest, Pageable pageable) {
        Page<Customer> page = customerRepository.searchCustomers(
                normalizeSearchValue(searchRequest.getCompanyName()),
                searchRequest.getStatus(),
                normalizeSearchValue(searchRequest.getVatNumber()),
                normalizeSearchValue(searchRequest.getEmail()),
                pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getPendingCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findPendingCustomers(pageable);
        return buildPageResponse(page);
    }

    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = findCustomerById(id);

        if (!customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email already in use: " + request.getEmail());
        }
        if (!customer.getVatNumber().equals(request.getVatNumber()) && customerRepository.existsByVatNumber(request.getVatNumber())) {
            throw new ResourceConflictException("VAT number already in use: " + request.getVatNumber());
        }

        customerMapper.updateEntity(customer, request);
        Customer updated = customerRepository.save(customer);

        log.info("Customer updated: {}", id);
        eventPublisher.publishCustomerUpdated(updated, List.of("companyName", "email", "phone"));

        return customerMapper.toResponse(updated);
    }

    public CustomerResponse validateCustomer(UUID id, UUID validatedBy) {
        Customer customer = findCustomerById(id);

        if (customer.getStatus() != CustomerStatus.PENDING) {
            throw new BadRequestException("Customer is not in PENDING status");
        }

        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setValidatedBy(validatedBy);
        customer.setValidatedAt(LocalDateTime.now());

        Customer validated = customerRepository.save(customer);
        log.info("Customer validated: {} by {}", id, validatedBy);

        eventPublisher.publishCustomerValidated(validated);

        return customerMapper.toResponse(validated);
    }

    public CustomerResponse updateStatus(UUID id, CustomerStatusUpdateRequest request, UUID updatedBy) {
        Customer customer = findCustomerById(id);
        CustomerStatus oldStatus = customer.getStatus();
        CustomerStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            throw new BadRequestException("New status is the same as current status");
        }

        validateStatusTransition(oldStatus, newStatus);

        customer.setStatus(newStatus);
        Customer updated = customerRepository.save(customer);

        log.info("Customer {} status changed from {} to {}", id, oldStatus, newStatus);

        switch (newStatus) {
            case SUSPENDED -> eventPublisher.publishCustomerSuspended(updated, request.getReason(), updatedBy);
            case ACTIVE -> {
                if (oldStatus == CustomerStatus.SUSPENDED) {
                    eventPublisher.publishCustomerReactivated(updated, updatedBy);
                }
            }
            case DELETED -> {
                customer.setDeletedAt(LocalDateTime.now());
                customerRepository.save(customer);
                eventPublisher.publishCustomerDeleted(updated, updatedBy);
            }
            default -> {}
        }

        return customerMapper.toResponse(updated);
    }

    public void deleteCustomer(UUID id, UUID deletedBy) {
        Customer customer = findCustomerById(id);

        if (customer.getStatus() == CustomerStatus.DELETED) {
            throw new BadRequestException("Customer is already deleted");
        }

        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Customer deleted: {}", id);
        eventPublisher.publishCustomerDeleted(customer, deletedBy);
    }

    @Transactional(readOnly = true)
    public Customer findCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public boolean customerExists(UUID id) {
        return customerRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean isCustomerActive(UUID id) {
        return customerRepository.findById(id)
                .map(Customer::isActive)
                .orElse(false);
    }

    private void validateStatusTransition(CustomerStatus oldStatus, CustomerStatus newStatus) {
        if (oldStatus == CustomerStatus.DELETED) {
            throw new BadRequestException("Cannot change status of a deleted customer");
        }
        if (newStatus == CustomerStatus.PENDING) {
            throw new BadRequestException("Cannot revert to PENDING status");
        }
    }

    private PageResponse<CustomerResponse> buildPageResponse(Page<Customer> page) {
        return PageResponse.<CustomerResponse>builder()
                .content(customerMapper.toResponseList(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private String normalizeSearchValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}
