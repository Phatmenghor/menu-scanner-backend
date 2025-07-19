package com.emenu.config;

import com.emenu.enums.*;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.repository.RoleRepository;
import com.emenu.features.usermanagement.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @PostConstruct
    @Transactional
    public void initializeData() {
        log.info("Starting data initialization...");
        
        initializeRoles();
        
        if (createDefaultAdmin) {
            initializeDefaultUsers();
        }
        
        log.info("Data initialization completed.");
    }

    private void initializeRoles() {
        log.info("Initializing system roles...");
        
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {}", roleEnum.name());
            }
        });
        
        log.info("System roles initialization completed.");
    }

    private void initializeDefaultUsers() {
        log.info("Initializing default users...");
        
        // Create platform owner
        createPlatformOwner();
        
        // Create demo business owner
        createDemoBusinessOwner();
        
        // Create demo customer
        createDemoCustomer();
        
        log.info("Default users initialization completed.");
    }

    private void createPlatformOwner() {
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
        }
    }

    private void createDemoBusinessOwner() {
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
        }
    }

    private void createDemoCustomer() {
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
        }
    }
}
