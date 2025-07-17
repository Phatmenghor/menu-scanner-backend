package com.emenu.config;

import com.emenu.enumations.RoleEnum;
import com.emenu.feature.auth.models.Role;
import com.emenu.feature.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run before DefaultUserInitializer
public class DefaultRoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing roles...");

        if (roleRepository.count() == 0) {
            log.info("No roles found, creating default roles");
            roleRepository.save(new Role(RoleEnum.DEVELOPER));
            roleRepository.save(new Role(RoleEnum.ADMIN));
            roleRepository.save(new Role(RoleEnum.STAFF));
            roleRepository.save(new Role(RoleEnum.TEACHER));
            roleRepository.save(new Role(RoleEnum.STUDENT));
            log.info("Default roles created successfully");
        } else {
            log.info("Roles already exist, skipping initialization");
        }
    }
}