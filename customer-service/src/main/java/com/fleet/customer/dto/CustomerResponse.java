package com.fleet.customer.dto;

import com.fleet.customer.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String companyName;
    private String vatNumber;
    private String email;
    private String phone;
    private AddressResponse address;
    private String contactFirstName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;
    private CustomerStatus status;
    private UUID validatedBy;
    private LocalDateTime validatedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
