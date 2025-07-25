package com.emenu.config;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.subscription.service.SubscriptionPlanService;
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
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DataInitializationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionPlanService subscriptionPlanService;

    // Prevent multiple executions
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    @Value("${app.init.create-admin:true}")
    private boolean createDefaultAdmin;

    @Value("${app.init.admin-email:admin@emenu-platform.com}")
    private String defaultAdminEmail;

    @Value("${app.init.admin-password:Admin123!@#}")
    private String defaultAdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        // Prevent multiple executions
        if (!initialized.compareAndSet(false, true)) {
            log.warn("Data initialization already completed or in progress. Skipping...");
            return;
        }

        try {
            log.info("Starting data initialization...");

            // Initialize in order
            ensureRolesExist();
            initializeSubscriptionPlans();

            if (createDefaultAdmin) {
                initializeDefaultUsers();
            }

            log.info("Data initialization completed successfully.");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            // Reset flag on failure so it can be retried
            initialized.set(false);
            throw e;
        }
    }

    private void ensureRolesExist() {
        try {
            log.info("Ensuring system roles exist...");

            // Get all existing roles from database in one query
            List<RoleEnum> existingRoles = roleRepository.findAll()
                    .stream()
                    .map(Role::getName) // Adjust this method name to match your Role entity
                    .toList();

            // Get all enum values
            List<RoleEnum> allEnumRoles = Arrays.asList(RoleEnum.values());

            // Find missing roles by comparing enum values with database roles
            List<RoleEnum> missingRoles = allEnumRoles.stream()
                    .filter(roleEnum -> !existingRoles.contains(roleEnum))
                    .toList();

            if (missingRoles.isEmpty()) {
                log.info("All {} system roles already exist in database: {}",
                        allEnumRoles.size(), existingRoles);
            } else {
                log.info("Found {} missing roles out of {}: {}",
                        missingRoles.size(), allEnumRoles.size(), missingRoles);
                log.info("Existing roles in database: {}", existingRoles);
                createMissingRoles(missingRoles);
            }

            log.info("System roles verification completed.");
        } catch (Exception e) {
            log.error("Error during roles verification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ensure roles exist", e);
        }
    }

    private void createMissingRoles(List<RoleEnum> missingRoles) {
        missingRoles.forEach(roleEnum -> {
            try {
                Role role = new Role(roleEnum);
                role = roleRepository.save(role);
                log.info("Successfully created role: {} with ID: {}", roleEnum.name(), role.getId());
            } catch (Exception e) {
                log.error("Error creating role {}: {}", roleEnum.name(), e.getMessage());
                throw new RuntimeException("Failed to create role: " + roleEnum.name(), e);
            }
        });
        log.info("Successfully created {} missing roles", missingRoles.size());
    }

    private void initializeSubscriptionPlans() {
        try {
            log.info("Initializing subscription plans...");
            subscriptionPlanService.seedDefaultPlans();
            log.info("Subscription plans initialization completed.");
        } catch (Exception e) {
            log.error("Error initializing subscription plans: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize subscription plans", e);
        }
    }

    private void initializeDefaultUsers() {
        try {
            log.info("Initializing default users...");
            createPlatformOwner();
            createDemoBusinessOwner();
            createDemoCustomer();
            createTestAccounts(); // For testing different account statuses
            log.info("Default users initialization completed.");
        } catch (Exception e) {
            log.error("Error initializing default users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize default users", e);
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

                admin = userRepository.save(admin);
                log.info("Created platform owner: {} with ID: {}", defaultAdminEmail, admin.getId());
            } else {
                log.info("Platform owner already exists: {}", defaultAdminEmail);
            }
        } catch (Exception e) {
            log.error("Error creating platform owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create platform owner", e);
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

                businessOwner = userRepository.save(businessOwner);
                log.info("Created demo business owner: {} with ID: {}", businessEmail, businessOwner.getId());
            } else {
                log.info("Demo business owner already exists: {}", businessEmail);
            }
        } catch (Exception e) {
            log.error("Error creating demo business owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create demo business owner", e);
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

                customer = userRepository.save(customer);
                log.info("Created demo customer: {} with ID: {}", customerEmail, customer.getId());
            } else {
                log.info("Demo customer already exists: {}", customerEmail);
            }
        } catch (Exception e) {
            log.error("Error creating demo customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create demo customer", e);
        }
    }

    private void createTestAccounts() {
        try {
            log.info("Creating test accounts with different statuses...");

            // Inactive user
            createTestUser("inactive-user@emenu-platform.com", "Test", "Inactive",
                    AccountStatus.INACTIVE, RoleEnum.CUSTOMER);

            // Locked user
            createTestUser("locked-user@emenu-platform.com", "Test", "Locked",
                    AccountStatus.LOCKED, RoleEnum.CUSTOMER);

            // Suspended user
            createTestUser("suspended-user@emenu-platform.com", "Test", "Suspended",
                    AccountStatus.SUSPENDED, RoleEnum.BUSINESS_OWNER);

        } catch (Exception e) {
            log.error("Error creating test accounts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test accounts", e);
        }
    }

    private void createTestUser(String email, String firstName, String lastName,
                                AccountStatus status, RoleEnum roleEnum) {
        try {
            if (!userRepository.existsByEmailAndIsDeletedFalse(email)) {
                User user = new User();
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("Test123!"));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setUserType(roleEnum.isCustomerRole() ? UserType.CUSTOMER :
                        roleEnum.isBusinessRole() ? UserType.BUSINESS_USER : UserType.PLATFORM_USER);
                user.setAccountStatus(status);

                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum));
                user.setRoles(List.of(role));

                user = userRepository.save(user);
                log.info("Created test user: {} with status: {} and ID: {}", email, status, user.getId());
            } else {
                log.info("Test user already exists: {}", email);
            }
        } catch (Exception e) {
            log.error("Error creating test user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to create test user: " + email, e);
        }
    }
}