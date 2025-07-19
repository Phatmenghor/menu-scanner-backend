package com.emenu.config;

import com.emenu.enums.*;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DatabaseInitializer
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

            // Ensure roles exist first
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

            // Create platform owner
            createPlatformOwner();

            // Create demo business owner
            createDemoBusinessOwner();

            // Create demo customer
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
                admin.setAccountStatus(AccountStatus.ACTIVE);
                admin.setEmailVerified(true);
                admin.setPhoneVerified(false);
                admin.setEmployeeId("EMP001");
                admin.setDepartment("Administration");
                admin.setHireDate(LocalDate.now());
                admin.setCompany("E-Menu Platform Inc.");
                admin.setPosition("Platform Owner");
                admin.setCountry("United States");
                admin.setCity("San Francisco");
                admin.setTimezone("America/Los_Angeles");
                admin.setLanguage("en");
                admin.setCurrency("USD");
                admin.acceptTermsAndPrivacy();

                // Set platform owner role
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
                businessOwner.setEmailVerified(true);
                businessOwner.setPhoneNumber("+1234567890");
                businessOwner.setPhoneVerified(true);
                businessOwner.setCompany("Demo Restaurant");
                businessOwner.setPosition("Owner");
                businessOwner.setCountry("United States");
                businessOwner.setCity("New York");
                businessOwner.setState("NY");
                businessOwner.setPostalCode("10001");
                businessOwner.setAddress("123 Demo Street");
                businessOwner.setTimezone("America/New_York");
                businessOwner.setLanguage("en");
                businessOwner.setCurrency("USD");
                businessOwner.acceptTermsAndPrivacy();

                // Set subscription
                businessOwner.setSubscriptionPlan(SubscriptionPlan.PROFESSIONAL);
                businessOwner.setSubscriptionStarts(LocalDateTime.now());
                businessOwner.setSubscriptionEnds(LocalDateTime.now().plusDays(365));

                // Set business owner role
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
                customer.setEmailVerified(true);
                customer.setPhoneNumber("+1987654321");
                customer.setPhoneVerified(true);
                customer.setCountry("United States");
                customer.setCity("Los Angeles");
                customer.setState("CA");
                customer.setPostalCode("90210");
                customer.setTimezone("America/Los_Angeles");
                customer.setLanguage("en");
                customer.setCurrency("USD");
                customer.setCustomerTier(CustomerTier.GOLD);
                customer.setLoyaltyPoints(750);
                customer.setTotalOrders(15);
                customer.setTotalSpent(450.75);
                customer.acceptTermsAndPrivacy();

                // Set customer role
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