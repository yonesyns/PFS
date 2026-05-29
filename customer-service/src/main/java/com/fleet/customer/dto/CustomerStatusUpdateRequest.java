package com.fleet.customer.dto;

import com.fleet.customer.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private CustomerStatus status;

    private String reason;
}
