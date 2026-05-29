package com.fleet.auth.service;

import com.fleet.auth.dto.AuthResponse;
import com.fleet.auth.dto.LoginRequest;
import com.fleet.auth.dto.RegisterRequest;
import com.fleet.auth.dto.UserResponse;
import com.fleet.auth.entity.Role;
import com.fleet.auth.entity.UserAccount;
import com.fleet.auth.repository.UserRepository;
import com.fleet.commons.exception.ResourceConflictException;
import com.fleet.commons.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email already exists: " + email);
        }

        UserAccount user = UserAccount.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(Role.CUSTOMER_ADMIN)
                .enabled(true)
                .build();

        return toAuthResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Unknown user"));
        return toUserResponse(user);
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateToken(user))
                .expiresIn(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(List.of(user.getRole().name()))
                .build();
    }

    private UserResponse toUserResponse(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
