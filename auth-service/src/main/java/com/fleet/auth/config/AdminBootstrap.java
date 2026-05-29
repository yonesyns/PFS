package com.fleet.auth.config;

import com.fleet.auth.entity.Role;
import com.fleet.auth.entity.UserAccount;
import com.fleet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createDefaultAdmin(
            @Value("${fleet.auth.admin.email}") String email,
            @Value("${fleet.auth.admin.password}") String password,
            @Value("${fleet.auth.admin.first-name}") String firstName,
            @Value("${fleet.auth.admin.last-name}") String lastName) {
        return args -> {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (!userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                userRepository.save(UserAccount.builder()
                        .email(normalizedEmail)
                        .password(passwordEncoder.encode(password))
                        .firstName(firstName)
                        .lastName(lastName)
                        .role(Role.SUPER_ADMIN)
                        .enabled(true)
                        .build());
            }
        };
    }
}
