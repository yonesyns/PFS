package com.fleet.customer.controller;

import com.fleet.customer.dto.BookingReference;
import com.fleet.customer.dto.CustomerDTO;
import com.fleet.customer.dto.CustomerSearchCriteria;
import com.fleet.customer.model.Customer;
import com.fleet.customer.model.CustomerStatus;
import com.fleet.customer.service.BookingClientService;
import com.fleet.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final BookingClientService bookingClientService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Customer> updateStatus(@PathVariable Long id, @RequestParam CustomerStatus status) {
        return ResponseEntity.ok(customerService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(CustomerSearchCriteria criteria) {
        return ResponseEntity.ok(customerService.search(criteria));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<BookingReference>> getBookings(
            @PathVariable Long id,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(bookingClientService.getCustomerBookings(id, status));
    }
}
