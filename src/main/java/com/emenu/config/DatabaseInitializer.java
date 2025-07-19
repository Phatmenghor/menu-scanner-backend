package com.emenu.config;

import com.emenu.enums.RoleEnum;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing database with required data...");
        initializeRoles();
        log.info("Database initialization completed.");
    }

    private void initializeRoles() {
        log.info("Checking and creating system roles...");

        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {} with permissions: {}",
                        roleEnum.name(), role.getPermissions());
            } else {
                log.debug("Role already exists: {}", roleEnum.name());
            }
        });

        log.info("System roles initialization completed. Total roles: {}", roleRepository.count());
    }
}