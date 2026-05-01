package com.fleet.customer.dto;

import com.fleet.customer.model.Address;
import com.fleet.customer.model.CustomerStatus;
import com.fleet.customer.model.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number is invalid")
    private String phone;

    private Address address;

    private CustomerStatus status;
    private CustomerType type;
    private String photoUrl;
}
