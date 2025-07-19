package com.emenu.config;

import com.emenu.enums.RoleEnum;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("Initializing database with required data...");
            initializeRoles();
            log.info("Database initialization completed.");
        } catch (Exception e) {
            log.error("Error during database initialization: {}", e.getMessage(), e);
            // Don't fail the application startup for initialization errors
        }
    }

    private void initializeRoles() {
        try {
            log.info("Checking and creating system roles...");

            Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
                try {
                    if (!roleRepository.existsByName(roleEnum)) {
                        Role role = new Role(roleEnum);
                        roleRepository.save(role);
                        log.info("Created role: {} with permissions: {}",
                                roleEnum.name(), role.getPermissions());
                    } else {
                        log.debug("Role already exists: {}", roleEnum.name());
                    }
                } catch (Exception e) {
                    log.error("Error creating role {}: {}", roleEnum.name(), e.getMessage(), e);
                }
            });

            log.info("System roles initialization completed. Total roles: {}", roleRepository.count());
        } catch (Exception e) {
            log.error("Error during roles initialization: {}", e.getMessage(), e);
            throw e;
        }
    }
}