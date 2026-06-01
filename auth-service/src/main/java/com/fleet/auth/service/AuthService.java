package com.fleet.auth.service;

import com.fleet.auth.dto.*;
import com.fleet.auth.entity.UserAccount;
import com.fleet.auth.entity.UserRole;
import com.fleet.auth.entity.UserStatus;
import com.fleet.auth.repository.UserAccountRepository;
import com.fleet.auth.security.FleetUserDetails;
import com.fleet.auth.security.JwtService;
import com.fleet.commons.exception.BadRequestException;
import com.fleet.commons.exception.ResourceConflictException;
import com.fleet.commons.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == UserRole.SUPER_ADMIN || request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin users must be created by configuration or database migration");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResourceConflictException("Email already in use: " + request.getEmail());
        }

        UserAccount user = UserAccount.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER_USER)
                .status(UserStatus.ACTIVE)
                .customerId(request.getCustomerId())
                .build();

        return buildAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        FleetUserDetails principal = (FleetUserDetails) authentication.getPrincipal();
        UserAccount user = userRepository.findById(principal.getId())
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Claims claims = jwtService.parseToken(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());
        UserAccount user = userRepository.findById(userId)
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof FleetUserDetails principal)) {
            throw new UnauthorizedException("Authentication required");
        }

        UserAccount user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        return toResponse(user);
    }

    private AuthResponse buildAuthResponse(UserAccount user) {
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .expiresInSeconds(jwtService.getAccessTokenExpirationSeconds())
                .user(toResponse(user))
                .build();
    }

    private UserResponse toResponse(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .customerId(user.getCustomerId())
                .build();
    }
}
