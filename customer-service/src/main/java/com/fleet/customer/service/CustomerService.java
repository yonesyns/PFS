package com.fleet.customer.service;

import com.fleet.customer.dto.CustomerDTO;
import com.fleet.customer.dto.CustomerSearchCriteria;
import com.fleet.customer.model.Customer;
import com.fleet.customer.model.CustomerStatus;
import com.fleet.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    public Customer createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        if (dto.getPhone() != null && customerRepository.existsByPhone(dto.getPhone()))
            throw new IllegalArgumentException("Phone already exists: " + dto.getPhone());

        Customer customer = toEntity(new Customer(), dto);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = getCustomerById(id);

        if (!customer.getEmail().equals(dto.getEmail()) && customerRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        if (dto.getPhone() != null && !dto.getPhone().equals(customer.getPhone()) && customerRepository.existsByPhone(dto.getPhone()))
            throw new IllegalArgumentException("Phone already exists: " + dto.getPhone());

        return customerRepository.save(toEntity(customer, dto));
    }

    public Customer updateStatus(Long id, CustomerStatus status) {
        Customer customer = getCustomerById(id);
        customer.setStatus(status);
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public List<Customer> search(CustomerSearchCriteria criteria) {
        return customerRepository.search(
                criteria.getName(), criteria.getEmail(), criteria.getPhone(),
                criteria.getStatus(), criteria.getType(), criteria.getCity()
        );
    }

    private Customer toEntity(Customer customer, CustomerDTO dto) {
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getStatus() != null) customer.setStatus(dto.getStatus());
        if (dto.getType() != null) customer.setType(dto.getType());
        return customer;
    }
}
