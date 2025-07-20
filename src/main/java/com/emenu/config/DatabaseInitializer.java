package com.emenu.config;

import com.emenu.features.auth.models.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {}", roleEnum.name());
            }
        });
    }
}