package com.conel.market.config;

import com.conel.market.entity.role.Role;
import com.conel.market.repository.role.RoleRepository;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
@Transactional
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data seeding...");
        seedRoles();
        seedSuperAdmin();
        log.info("Data seeding completed");
    }

    private void seedRoles() {
        if (!roleRepository.existsByName("CUSTOMER")) {
            Role customerRole = Role.builder()
                    .name("CUSTOMER")
                    .build();
            roleRepository.save(customerRole);
            log.info("Created CUSTOMER role");
        } else {
            log.debug("CUSTOMER role already exists");
        }

        if (!roleRepository.existsByName("VENDOR")) {
            Role vendorRole = Role.builder()
                    .name("VENDOR")
                    .build();
            roleRepository.save(vendorRole);
            log.info("Created VENDOR role");
        } else {
            log.debug("VENDOR role already exists");
        }

        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created ADMIN role");
        } else {
            log.debug("ADMIN role already exists");
        }

        if (!roleRepository.existsByName("SUPER_ADMIN")) {
            Role superAdminRole = Role.builder()
                    .name("SUPER_ADMIN")
                    .build();
            roleRepository.save(superAdminRole);
            log.info("Created SUPER_ADMIN role");
        } else {
            log.debug("SUPER_ADMIN role already exists");
        }
    }

    private void seedSuperAdmin() {
        boolean superAdminExists = userRepository.existsByEmailIgnoreCase(adminEmail);

        if (superAdminExists) {
            log.debug("Super admin user already exists");
            return;
        }

        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        User superAdmin = User.builder()
                .firstName("System")
                .lastName("Administrator")
                .email(adminEmail)
                .phoneNumber(adminPhone)
                .password(passwordEncoder.encode(adminPassword))
                .dateOfBirth(LocalDate.of(2004, 5, 31))
                .emailVerified(true)
                .phoneVerified(true)
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .deleted(false)
                .vendorStatus(null)
                .vendorApproved(false)
                .roles(List.of(superAdminRole))
                .build();

        userRepository.save(superAdmin);
        // Never log secrets in production; the account is seeded but the password stays private.
        log.info("Created SUPER_ADMIN user: {} (password omitted)", adminEmail);
    }
}