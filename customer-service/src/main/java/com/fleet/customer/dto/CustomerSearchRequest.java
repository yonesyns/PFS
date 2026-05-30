package com.fleet.customer.dto;

import com.fleet.customer.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchRequest {
    private String companyName;
    private CustomerStatus status;
    private String vatNumber;
    private String email;
}
