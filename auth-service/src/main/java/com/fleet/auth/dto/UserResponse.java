package com.fleet.auth.dto;

import com.fleet.auth.entity.UserRole;
import com.fleet.auth.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private UUID customerId;
}
