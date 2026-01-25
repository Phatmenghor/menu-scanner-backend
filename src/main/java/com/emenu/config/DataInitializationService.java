package com.emenu.config;

import com.emenu.enums.user.AccountStatus;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionPlanService subscriptionPlanService;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Object initLock = new Object();

    @Value("${app.init.create-admin:true}")
    private boolean createDefaultAdmin;

    @Value("${app.init.admin-email:phatmenghor19@gmail.com}")
    private String defaultAdminEmail;

    @Value("${app.init.admin-password:88889999}")
    private String defaultAdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        // ✅ ENHANCED: Double-checked locking pattern for thread safety
        if (initialized.get()) {
            log.info("Data initialization already completed. Skipping...");
            return;
        }

        synchronized (initLock) {
            if (initialized.get()) {
                log.info("Data initialization already completed (double-check). Skipping...");
                return;
            }

            try {
                log.info("🚀 Starting Cambodia E-Menu Platform data initialization...");

                // Initialize in strict order
                int rolesCreated = ensureRolesExist();
                log.info("✅ Roles initialization completed - {} roles processed", rolesCreated);


                if (createDefaultAdmin) {
                    int usersCreated = initializeDefaultUsers();
                    log.info("✅ Default users initialization completed - {} users processed", usersCreated);
                }

                // Mark as initialized only after all steps complete
                initialized.set(true);
                log.info("🎉 Cambodia E-Menu Platform data initialization completed successfully!");

            } catch (Exception e) {
                log.error("❌ Error during data initialization: {}", e.getMessage(), e);
                // Don't set initialized flag on failure so it can be retried
                throw new RuntimeException("Data initialization failed", e);
            }
        }
    }

    private int ensureRolesExist() {
        try {
            log.info("🔄 Ensuring system roles exist...");

            String[] systemRoles = {"PLATFORM_OWNER", "BUSINESS_OWNER", "CUSTOMER"};
            int createdCount = 0;

            for (String roleName : systemRoles) {
                if (!roleRepository.existsByNameAndIsDeletedFalse(roleName)) {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDisplayName(roleName.replace("_", " "));
                    role.setDescription("System role: " + roleName);
                    role.setBusinessId(null);
                    roleRepository.save(role);
                    createdCount++;
                    log.info("✅ Created system role: {}", roleName);
                }
            }

            if (createdCount > 0) {
                log.info("✅ Created {} system roles", createdCount);
            } else {
                log.info("✅ All system roles already exist");
            }

            return systemRoles.length;

        } catch (Exception e) {
            log.error("❌ Error during roles verification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ensure roles exist", e);
        }
    }

    @SuppressWarnings("unused")
    private int createMissingRoles_DEPRECATED(List<String> missingRoles) {
        int createdCount = 0;

        for (String roleName : missingRoles) {
            try {
                if (roleRepository.existsByNameAndIsDeletedFalse(roleName)) {
                    log.debug("Role {} already exists (created by another process)", roleName);
                    continue;
                }

                Role role = new Role();
                role.setName(roleName);
                role.setDisplayName(roleName.replace("_", " "));
                role.setDescription("System role: " + roleName);
                roleRepository.save(role);
                createdCount++;
                
                log.info("✅ Successfully created role: {} with ID: {}", roleName, role.getId());
                
            } catch (Exception e) {
                // ✅ ENHANCED: Handle potential constraint violations gracefully
                if (e.getMessage() != null && e.getMessage().contains("constraint")) {
                    log.warn("⚠️ Role {} likely already exists (constraint violation), continuing...", roleName);
                    continue;
                } else {
                    log.error("❌ Error creating role {}: {}", roleName, e.getMessage());
                    throw new RuntimeException("Failed to create role: " + roleName, e);
                }
            }
        }
        
        return createdCount;
    }

    private int initializeDefaultUsers() {
        try {
            log.info("🔄 Initializing default users...");
            
            int usersCreated = 0;
            usersCreated += createPlatformOwner();
            usersCreated += createDemoBusinessOwner();
            usersCreated += createDemoCustomer();
            usersCreated += createTestAccounts();
            
            return usersCreated;
            
        } catch (Exception e) {
            log.error("❌ Error initializing default users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize default users", e);
        }
    }

    private int createPlatformOwner() {
        try {
            String adminUserIdentifier = defaultAdminEmail;

            if (!userRepository.existsByUserIdentifierAndIsDeletedFalse(adminUserIdentifier)) {
                User admin = new User();
                admin.setUserIdentifier(adminUserIdentifier);
                admin.setEmail(defaultAdminEmail);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setFirstName("Platform");
                admin.setLastName("Administrator");
                admin.setUserType(UserType.PLATFORM_USER);
                admin.setPosition("Platform Owner");
                admin.setAccountStatus(AccountStatus.ACTIVE);

                Role platformOwnerRole = roleRepository.findByNameAndIsDeletedFalse("PLATFORM_OWNER")
                        .orElseThrow(() -> new RuntimeException("Platform owner role not found"));
                admin.setRoles(List.of(platformOwnerRole));

                admin = userRepository.save(admin);
                log.info("✅ Created platform owner: {} with ID: {}", adminUserIdentifier, admin.getId());
                return 1;
            } else {
                log.info("ℹ️ Platform owner already exists: {}", adminUserIdentifier);
                return 0;
            }
        } catch (Exception e) {
            log.error("❌ Error creating platform owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create platform owner", e);
        }
    }

    private int createDemoBusinessOwner() {
        try {
            String businessUserIdentifier = "demo-business-owner";
            if (!userRepository.existsByUserIdentifierAndIsDeletedFalse(businessUserIdentifier)) {
                User businessOwner = new User();
                businessOwner.setUserIdentifier(businessUserIdentifier);
                businessOwner.setEmail("demo-business@emenu-platform.com");
                businessOwner.setPassword(passwordEncoder.encode("88889999"));
                businessOwner.setFirstName("Demo");
                businessOwner.setLastName("Restaurant Owner");
                businessOwner.setUserType(UserType.BUSINESS_USER);
                businessOwner.setAccountStatus(AccountStatus.ACTIVE);
                businessOwner.setPhoneNumber("+1234567890");
                businessOwner.setPosition("Owner");
                businessOwner.setAddress("123 Demo Street");

                Role businessOwnerRole = roleRepository.findByNameAndIsDeletedFalse("BUSINESS_OWNER")
                        .orElseThrow(() -> new RuntimeException("Business owner role not found"));
                businessOwner.setRoles(List.of(businessOwnerRole));

                businessOwner = userRepository.save(businessOwner);
                log.info("✅ Created demo business owner: {} with ID: {}", businessUserIdentifier, businessOwner.getId());
                return 1;
            } else {
                log.info("ℹ️ Demo business owner already exists: {}", businessUserIdentifier);
                return 0;
            }
        } catch (Exception e) {
            log.error("❌ Error creating demo business owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create demo business owner", e);
        }
    }

    private int createDemoCustomer() {
        try {
            String customerUserIdentifier = "demo-customer";
            if (!userRepository.existsByUserIdentifierAndIsDeletedFalse(customerUserIdentifier)) {
                User customer = new User();
                customer.setUserIdentifier(customerUserIdentifier);
                customer.setEmail("demo-customer@emenu-platform.com");
                customer.setPassword(passwordEncoder.encode("88889999"));
                customer.setFirstName("Demo");
                customer.setLastName("Customer");
                customer.setUserType(UserType.CUSTOMER);
                customer.setAccountStatus(AccountStatus.ACTIVE);
                customer.setPhoneNumber("+1987654321");

                Role customerRole = roleRepository.findByNameAndIsDeletedFalse("CUSTOMER")
                        .orElseThrow(() -> new RuntimeException("Customer role not found"));
                customer.setRoles(List.of(customerRole));

                customer = userRepository.save(customer);
                log.info("✅ Created demo customer: {} with ID: {}", customerUserIdentifier, customer.getId());
                return 1;
            } else {
                log.info("ℹ️ Demo customer already exists: {}", customerUserIdentifier);
                return 0;
            }
        } catch (Exception e) {
            log.error("❌ Error creating demo customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create demo customer", e);
        }
    }

    private int createTestAccounts() {
        try {
            log.info("🔄 Creating test accounts with different statuses...");

            int created = 0;
            created += createTestUser("inactive-user", "Test", "Inactive", AccountStatus.INACTIVE, "CUSTOMER");
            created += createTestUser("locked-user", "Test", "Locked", AccountStatus.LOCKED, "CUSTOMER");
            created += createTestUser("suspended-user", "Test", "Suspended", AccountStatus.SUSPENDED, "BUSINESS_OWNER");

            return created;
            
        } catch (Exception e) {
            log.error("❌ Error creating test accounts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create test accounts", e);
        }
    }

    private int createTestUser(String userIdentifier, String firstName, String lastName,
                              AccountStatus status, String roleName) {
        try {
            if (!userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier)) {
                User user = new User();
                user.setUserIdentifier(userIdentifier);
                user.setEmail(userIdentifier + "@emenu-platform.com");
                user.setPassword(passwordEncoder.encode("88889999"));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setUserType("CUSTOMER".equals(roleName) ? UserType.CUSTOMER :
                        roleName.startsWith("BUSINESS_") ? UserType.BUSINESS_USER : UserType.PLATFORM_USER);
                user.setAccountStatus(status);

                Role role = roleRepository.findByNameAndIsDeletedFalse(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                user.setRoles(List.of(role));

                user = userRepository.save(user);
                log.info("✅ Created test user: {} with status: {} and ID: {}", userIdentifier, status, user.getId());
                return 1;
            } else {
                log.info("ℹ️ Test user already exists: {}", userIdentifier);
                return 0;
            }
        } catch (Exception e) {
            log.error("❌ Error creating test user {}: {}", userIdentifier, e.getMessage(), e);
            throw new RuntimeException("Failed to create test user: " + userIdentifier, e);
        }
    }
}