package com.fleet.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    @NotBlank(message = "VAT number is required")
    @Size(min = 5, max = 50, message = "VAT number must be between 5 and 50 characters")
    private String vatNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private AddressRequest address;

    @Size(max = 50, message = "Contact first name must not exceed 50 characters")
    private String contactFirstName;

    @Size(max = 50, message = "Contact last name must not exceed 50 characters")
    private String contactLastName;

    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;
}
