package com.fleet.customer.dto;

import com.fleet.customer.model.CustomerStatus;
import com.fleet.customer.model.CustomerType;
import lombok.Data;

@Data
public class CustomerSearchCriteria {
    private String name;
    private String email;
    private String phone;
    private CustomerStatus status;
    private CustomerType type;
    private String city;
}
