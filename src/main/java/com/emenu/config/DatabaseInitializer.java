package com.emenu.config;

import com.emenu.enums.RoleEnum;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing database...");
        initializeRoles();
        log.info("Database initialization completed");
    }

    private void initializeRoles() {
        // Get all existing roles from database
        List<RoleEnum> existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName) // Assuming Role has getName() method that returns RoleEnum
                .toList();

        // Get all enum values
        List<RoleEnum> allEnumRoles = Arrays.asList(RoleEnum.values());

        // Find missing roles by comparing enum values with database roles
        List<RoleEnum> missingRoles = allEnumRoles.stream()
                .filter(roleEnum -> !existingRoles.contains(roleEnum))
                .toList();

        if (missingRoles.isEmpty()) {
            log.info("All roles already exist in database. No action needed.");
        } else {
            log.info("Found {} missing roles: {}", missingRoles.size(), missingRoles);
            createMissingRoles(missingRoles);
        }
    }

    private void createMissingRoles(List<RoleEnum> missingRoles) {
        missingRoles.forEach(roleEnum -> {
            Role role = new Role(roleEnum);
            roleRepository.save(role);
            log.info("Created role: {}", roleEnum.name());
        });
        log.info("Successfully created {} missing roles", missingRoles.size());
    }
}