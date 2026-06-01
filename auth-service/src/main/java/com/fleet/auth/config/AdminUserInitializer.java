package com.fleet.auth.config;

import com.fleet.auth.entity.UserAccount;
import com.fleet.auth.entity.UserRole;
import com.fleet.auth.entity.UserStatus;
import com.fleet.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fleet.security.admin.email:admin@fleet.com}")
    private String adminEmail;

    @Value("${fleet.security.admin.password:Admin123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        UserAccount admin = UserAccount.builder()
                .firstName("Fleet")
                .lastName("Admin")
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", normalizedEmail);
    }
}
