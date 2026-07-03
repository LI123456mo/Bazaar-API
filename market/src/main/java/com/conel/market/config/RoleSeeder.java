package com.conel.market.config;

import com.conel.market.entity.role.Role;
import com.conel.market.repository.role.RoleRepository;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data seeding...");
        seedRoles();
        seedSuperAdmin();
        log.info("Data seeding completed");
    }

    private void seedRoles() {
        roleRepository.findByName("CUSTOMER")
                .ifPresentOrElse(
                        role -> log.debug("customer role already exist"),
                        ()->{
                            Role customerRole= Role.builder()
                                    .name("CUSTOMER")
                                    .build();
                            roleRepository.save(customerRole);
                        }
                );


        roleRepository.findByName("VENDOR")
                .ifPresentOrElse(
                        role -> log.debug("VENDOR role already exist"),
                        ()->{
                            Role vendorRole=Role.builder()
                                    .name("VENDOR")
                                    .build();
                            roleRepository.save(vendorRole);
                            log.info("Created VENDOR role");
                        }
                );

        roleRepository.findByName("ADMIN")
                .ifPresentOrElse(
                        role -> log.debug("ADMIN role already exists"),
                        () -> {
                            Role adminRole = Role.builder()
                                    .name("ADMIN")
                                    .build();
                            roleRepository.save(adminRole);
                            log.info("Created ADMIN role");
                        }
                );


        roleRepository.findByName("SUPER_ADMIN")
                .ifPresentOrElse(
                        role -> log.debug("✓ SUPER_ADMIN role already exists"),
                        ()->{
                            Role superAdminRole=Role.builder()
                                    .name("SUPER_ADMIN")
                                    .build();
                            roleRepository.save(superAdminRole);
                            log.info("✓ Created SUPER_ADMIN role");
                        }
                );
    }

    private void seedSuperAdmin() {
        boolean superAdminExists=userRepository.existsByEmailIgnoreCase("conellimo@gmail.com");

        if (superAdminExists){
            log.debug("Super admin user already exists");
            return;
        }

        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        User superAdmin=User.builder()
                .firstName("System")
                .lastName("Administrator")
                .email("conellimo@gmail.com")
                .phoneNumber("0706440885")
                .password(passwordEncoder.encode("SuperAdmin@123"))
                .dateOfBirth(LocalDate.of(2004,5,31))
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
        log.info("Created SUPER_ADMIN user: conellimo@gamil.com (Password: SuperAdmin@123 — CHANGE IN PRODUCTION!)");
    }
}
