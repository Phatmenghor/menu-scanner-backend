package com.emenu.config;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DataInitializationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.create-admin:true}")
    private boolean createDefaultAdmin;

    @Value("${app.init.admin-email:admin@emenu-platform.com}")
    private String defaultAdminEmail;

    @Value("${app.init.admin-password:Admin123!@#}")
    private String defaultAdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        try {
            log.info("Starting data initialization...");
            ensureRolesExist();
            if (createDefaultAdmin) {
                initializeDefaultUsers();
            }
            log.info("Data initialization completed.");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
        }
    }

    private void ensureRolesExist() {
        try {
            log.info("Ensuring system roles exist...");
            Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
                try {
                    if (!roleRepository.existsByName(roleEnum)) {
                        Role role = new Role(roleEnum);
                        roleRepository.save(role);
                        log.info("Created missing role: {}", roleEnum.name());
                    }
                } catch (Exception e) {
                    log.error("Error ensuring role exists {}: {}", roleEnum.name(), e.getMessage());
                }
            });
            log.info("System roles verification completed.");
        } catch (Exception e) {
            log.error("Error during roles verification: {}", e.getMessage(), e);
        }
    }

    private void initializeDefaultUsers() {
        try {
            log.info("Initializing default users...");
            createPlatformOwner();
            createDemoBusinessOwner();
            createDemoCustomer();
            log.info("Default users initialization completed.");
        } catch (Exception e) {
            log.error("Error initializing default users: {}", e.getMessage(), e);
        }
    }

    private void createPlatformOwner() {
        try {
            if (!userRepository.existsByEmailAndIsDeletedFalse(defaultAdminEmail)) {
                User admin = new User();
                admin.setEmail(defaultAdminEmail);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setFirstName("Platform");
                admin.setLastName("Administrator");
                admin.setUserType(UserType.PLATFORM_USER);
                admin.setPosition("Platform Owner");
                admin.setAccountStatus(AccountStatus.ACTIVE);

                Role platformOwnerRole = roleRepository.findByName(RoleEnum.PLATFORM_OWNER)
                        .orElseThrow(() -> new RuntimeException("Platform owner role not found"));
                admin.setRoles(List.of(platformOwnerRole));

                userRepository.save(admin);
                log.info("Created platform owner: {}", defaultAdminEmail);
            } else {
                log.info("Platform owner already exists: {}", defaultAdminEmail);
            }
        } catch (Exception e) {
            log.error("Error creating platform owner: {}", e.getMessage(), e);
        }
    }

    private void createDemoBusinessOwner() {
        try {
            String businessEmail = "demo-business@emenu-platform.com";
            if (!userRepository.existsByEmailAndIsDeletedFalse(businessEmail)) {
                User businessOwner = new User();
                businessOwner.setEmail(businessEmail);
                businessOwner.setPassword(passwordEncoder.encode("Business123!"));
                businessOwner.setFirstName("Demo");
                businessOwner.setLastName("Restaurant Owner");
                businessOwner.setUserType(UserType.BUSINESS_USER);
                businessOwner.setAccountStatus(AccountStatus.ACTIVE);
                businessOwner.setPhoneNumber("+1234567890");
                businessOwner.setPosition("Owner");
                businessOwner.setAddress("123 Demo Street");

                Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                        .orElseThrow(() -> new RuntimeException("Business owner role not found"));
                businessOwner.setRoles(List.of(businessOwnerRole));

                userRepository.save(businessOwner);
                log.info("Created demo business owner: {}", businessEmail);
            } else {
                log.info("Demo business owner already exists: {}", businessEmail);
            }
        } catch (Exception e) {
            log.error("Error creating demo business owner: {}", e.getMessage(), e);
        }
    }

    private void createDemoCustomer() {
        try {
            String customerEmail = "demo-customer@emenu-platform.com";
            if (!userRepository.existsByEmailAndIsDeletedFalse(customerEmail)) {
                User customer = new User();
                customer.setEmail(customerEmail);
                customer.setPassword(passwordEncoder.encode("Customer123!"));
                customer.setFirstName("Demo");
                customer.setLastName("Customer");
                customer.setUserType(UserType.CUSTOMER);
                customer.setAccountStatus(AccountStatus.ACTIVE);
                customer.setPhoneNumber("+1987654321");

                Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Customer role not found"));
                customer.setRoles(List.of(customerRole));

                userRepository.save(customer);
                log.info("Created demo customer: {}", customerEmail);
            } else {
                log.info("Demo customer already exists: {}", customerEmail);
            }
        } catch (Exception e) {
            log.error("Error creating demo customer: {}", e.getMessage(), e);
        }
    }
}