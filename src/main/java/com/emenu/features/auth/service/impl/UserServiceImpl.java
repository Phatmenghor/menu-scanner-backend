package com.emenu.features.auth.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.BusinessSettingsRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.BusinessOwnerCreateResponseMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.service.BusinessSettingsService;
import com.emenu.features.auth.service.UserService;
import com.emenu.features.auth.specification.UserSpecification;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final BusinessService businessService;
    private final SubdomainService subdomainService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanRepository subscriptionPlanRepository; // ✅ ADDED: Subscription plan repository
    private final BusinessOwnerCreateResponseMapper businessOwnerResponseMapper;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {} with type: {}", request.getUserIdentifier(), request.getUserType());

        // ✅ UPDATED: Only validate userIdentifier uniqueness
        if (existsByUserIdentifier(request.getUserIdentifier())) {
            throw new ValidationException("User identifier already exists");
        }

        // ✅ REMOVED: No email/phone uniqueness validation for regular users

        try {
            // ✅ Create user entity
            User user = new User();
            user.setUserIdentifier(request.getUserIdentifier());
            user.setEmail(request.getEmail()); // Optional - can be null
            user.setPhoneNumber(request.getPhoneNumber()); // Optional - can be null
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setProfileImageUrl(request.getProfileImageUrl());
            user.setPosition(request.getPosition());
            user.setAddress(request.getAddress());
            user.setNotes(request.getNotes());
            user.setUserType(request.getUserType());
            user.setAccountStatus(request.getAccountStatus());

            // ✅ Handle business assignment with security checks
            if (request.getBusinessId() != null) {
                validateAndAssignBusiness(user, request.getBusinessId());
            }

            // ✅ Set and validate roles
            setUserRoles(user, request.getRoles());

            User savedUser = userRepository.save(user);
            log.info("User created successfully: {} with type: {}", savedUser.getUserIdentifier(), savedUser.getUserType());

            return userMapper.toResponse(savedUser);

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getAllUsers(UserFilterRequest request) {
        log.debug("Getting all users with filter - UserType: {}, AccountStatus: {}, BusinessId: {}",
                request.getUserType(), request.getAccountStatus(), request.getBusinessId());

        // ✅ Security: Business users can only see users from their business
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && request.getBusinessId() == null) {
            request.setBusinessId(currentUser.getBusinessId());
        }

        Specification<User> spec = UserSpecification.buildSearchSpecification(request);

        int pageNo = request.getPageNo() != null && request.getPageNo() > 0 ? request.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, request.getPageSize(), request.getSortBy(), request.getSortDirection()
        );

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userMapper.toPaginationResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.debug("Getting user by ID: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Handle business assignment changes
        if (request.getBusinessId() != null && !request.getBusinessId().equals(user.getBusinessId())) {
            validateAndAssignBusiness(user, request.getBusinessId());
        }

        // ✅ Update roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            setUserRoles(user, request.getRoles());
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getUserIdentifier());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Prevent self-deletion
        User currentUser = securityUtils.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new ValidationException("You cannot delete your own account");
        }

        user.softDelete();
        user = userRepository.save(user);
        log.info("User deleted: {}", user.getUserIdentifier());

        return userMapper.toResponse(user);
    }

    // ================================
    // CURRENT USER OPERATIONS
    // ================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        log.debug("Getting current user profile");
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        log.info("Updating current user profile");

        User currentUser = securityUtils.getCurrentUser();

        // ✅ Restricted update for current user (no sensitive fields)
        userMapper.updateCurrentUserProfile(request, currentUser);

        User updatedUser = userRepository.save(currentUser);
        log.info("Current user profile updated: {}", updatedUser.getUserIdentifier());

        return userMapper.toResponse(updatedUser);
    }

    // ================================
    // BUSINESS OWNER CREATION
    // ================================

    @Override
    public BusinessOwnerCreateResponse createBusinessOwner(BusinessOwnerCreateRequest request) {
        log.info("🚀 Creating comprehensive business owner with business: {} for userIdentifier: {}",
                request.getBusinessName(), request.getOwnerUserIdentifier());

        // ✅ ENHANCED: Validate business owner creation early
        validateBusinessOwnerCreation(request);

        try {
            // ✅ STEP 1: Create business with enhanced settings
            log.info("📊 Step 1: Creating business with settings: {}", request.getBusinessName());
            BusinessResponse businessResponse = createBusinessForOwnerEnhanced(request);

            // ✅ STEP 2: Create business owner
            log.info("👤 Step 2: Creating business owner: {}", request.getOwnerUserIdentifier());
            UserResponse userResponse = createOwnerUser(request, businessResponse.getId());

            // ✅ STEP 3: Create subdomain - FIXED: Handle errors properly without auto-generation
            log.info("🌐 Step 3: Creating subdomain: {}", request.getPreferredSubdomain());
            SubdomainResponse subdomainResponse;
            try {
                subdomainResponse = subdomainService.createSubdomainForBusiness(
                        businessResponse.getId(),
                        request.getPreferredSubdomain()
                );
                log.info("✅ Subdomain created successfully: {}", subdomainResponse.getFullUrl());
            } catch (ValidationException e) {
                log.error("❌ Subdomain creation failed: {}", e.getMessage());
                // ✅ FIXED: Don't continue with fallback, throw meaningful error
                throw new ValidationException("Subdomain creation failed: " + e.getMessage());
            } catch (Exception e) {
                log.error("❌ Unexpected error creating subdomain: {}", e.getMessage(), e);
                throw new ValidationException("Failed to create subdomain '" + request.getPreferredSubdomain() + "': " + e.getMessage());
            }

            // ✅ STEP 4: Create subscription (ALWAYS - never null)
            log.info("📋 Step 4: Creating subscription with plan: {}", request.getSubscriptionPlanId());
            SubscriptionResponse subscriptionResponse = createSubscriptionForBusiness(request, businessResponse.getId());

            // ✅ STEP 5: Create payment if requested (OPTIONAL)
            PaymentResponse paymentResponse = null;
            if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
                log.info("💳 Step 5: Creating payment record: ${}", request.getPaymentAmount());
                try {
                    paymentResponse = createPaymentForBusiness(request, businessResponse.getId(), subscriptionResponse);
                    log.info("✅ Payment created successfully: {}", paymentResponse.getId());
                } catch (Exception e) {
                    log.warn("⚠️ Payment creation failed, continuing without payment: {}", e.getMessage());
                    // Continue without payment - don't fail the entire process
                }
            }

            // ✅ STEP 6: Use mapper to create comprehensive response
            BusinessOwnerCreateResponse response = businessOwnerResponseMapper.create(
                    userResponse,
                    businessResponse,
                    subdomainResponse,
                    subscriptionResponse, // Never null
                    paymentResponse       // Can be null
            );

            log.info("✅ Comprehensive business owner creation completed successfully: {}", userResponse.getUserIdentifier());
            log.info("📋 {}", response.getSummary());

            return response;

        } catch (ValidationException ve) {
            log.error("❌ Validation error creating business owner: {}", ve.getMessage());
            throw ve; // Re-throw validation exceptions as-is
        } catch (Exception e) {
            log.error("❌ Failed to create comprehensive business owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create business owner: " + e.getMessage(), e);
        }
    }


    // ✅ NEW: Enhanced business creation with settings
    private BusinessResponse createBusinessForOwnerEnhanced(BusinessOwnerCreateRequest request) {
        BusinessCreateRequest businessRequest = new BusinessCreateRequest();
        businessRequest.setName(request.getBusinessName());
        businessRequest.setEmail(request.getBusinessEmail());
        businessRequest.setPhone(request.getBusinessPhone());
        businessRequest.setAddress(request.getBusinessAddress());
        businessRequest.setDescription(request.getBusinessDescription());

        BusinessResponse businessResponse = businessService.createBusiness(businessRequest);

        log.info("✅ Enhanced business created: {} with ID: {}", businessResponse.getName(), businessResponse.getId());
        return businessResponse;
    }

    private void validateBusinessOwnerCreation(BusinessOwnerCreateRequest request) {
        log.debug("🔍 Validating business owner creation request");

        List<String> errors = new ArrayList<>();

        // ✅ Check userIdentifier uniqueness
        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(request.getOwnerUserIdentifier())) {
            errors.add("Owner user identifier '" + request.getOwnerUserIdentifier() + "' is already taken");
        }

        // ✅ Check subdomain availability
        if (!subdomainService.isSubdomainAvailable(request.getPreferredSubdomain())) {
            errors.add("Subdomain '" + request.getPreferredSubdomain() + "' is not available. Please choose a different subdomain");
        }

        // ✅ Validate subscription plan exists
        if (!subscriptionPlanRepository.existsById(request.getSubscriptionPlanId())) {
            errors.add("Subscription plan not found");
        }

        // ✅ Validate payment info if provided
        if (request.hasPaymentInfo() && !request.isPaymentInfoComplete()) {
            errors.add("Payment method is required when payment amount is provided");
        }

        // ✅ Throw single validation exception with all errors
        if (!errors.isEmpty()) {
            String errorMessage = "Business owner creation validation failed: " + String.join(", ", errors);
            throw new ValidationException(errorMessage);
        }

        log.debug("✅ Validation passed for business owner creation");
    }

    private SubscriptionResponse createSubscriptionForBusiness(BusinessOwnerCreateRequest request, UUID businessId) {
        // ✅ ADDED: Validate subscription plan ID is not null
        if (request.getSubscriptionPlanId() == null) {
            throw new ValidationException("Subscription plan ID cannot be null");
        }

        // ✅ ADDED: Validate subscription plan exists
        log.debug("🔍 Validating subscription plan exists: {}", request.getSubscriptionPlanId());
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(request.getSubscriptionPlanId())
                .orElseThrow(() -> new ValidationException("Subscription plan not found with ID: " + request.getSubscriptionPlanId()));

        log.info("✅ Subscription plan validated: {} - {}", subscriptionPlan.getId(), subscriptionPlan.getName());

        SubscriptionCreateRequest subscriptionRequest = new SubscriptionCreateRequest();
        subscriptionRequest.setBusinessId(businessId);
        subscriptionRequest.setPlanId(request.getSubscriptionPlanId()); // Now safe to use
        subscriptionRequest.setStartDate(request.getSubscriptionStartDate()); // ✅ FIXED: Now uses LocalDate
        subscriptionRequest.setAutoRenew(request.getAutoRenew());

        return subscriptionService.createSubscription(subscriptionRequest);
    }

    // ✅ NEW: Create payment for the business
    private PaymentResponse createPaymentForBusiness(BusinessOwnerCreateRequest request, UUID businessId, SubscriptionResponse subscription) {
        PaymentCreateRequest paymentRequest = new PaymentCreateRequest();
        paymentRequest.setImageUrl(request.getPaymentImageUrl());
        paymentRequest.setAmount(request.getPaymentAmount());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setStatus(request.getPaymentStatus());
        paymentRequest.setReferenceNumber(request.getPaymentReferenceNumber());
        paymentRequest.setNotes(request.getPaymentNotes());

        // ✅ Link payment to subscription if available, otherwise to business
        if (subscription != null) {
            paymentRequest.setSubscriptionId(subscription.getId());
            paymentRequest.setPaymentType(PaymentType.SUBSCRIPTION);
        } else {
            paymentRequest.setBusinessId(businessId);
            paymentRequest.setPaymentType(PaymentType.BUSINESS_RECORD);
        }

        return paymentService.createPayment(paymentRequest);
    }

    private UserResponse createOwnerUser(BusinessOwnerCreateRequest request, UUID businessId) {
        User user = new User();

        // ✅ UPDATED: Use ownerUserIdentifier instead of email
        user.setUserIdentifier(request.getOwnerUserIdentifier());
        user.setEmail(request.getOwnerEmail());
        user.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
        user.setFirstName(request.getOwnerFirstName());
        user.setLastName(request.getOwnerLastName());
        user.setPhoneNumber(request.getOwnerPhone());
        user.setAddress(request.getOwnerAddress());
        user.setUserType(UserType.BUSINESS_USER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBusinessId(businessId);
        user.setPosition("Owner");

        // Set business owner role
        Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                .orElseThrow(() -> new ValidationException("Business owner role not found"));
        user.setRoles(List.of(businessOwnerRole));

        User savedUser = userRepository.save(user);
        log.info("✅ Business owner created successfully: {} for business ID: {}",
                savedUser.getUserIdentifier(), businessId);

        return userMapper.toResponse(savedUser);
    }

    private SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain) {
        try {
            log.info("🌐 Creating subdomain for business: {} with preferred: {}", businessId, preferredSubdomain);

            SubdomainResponse subdomainResponse = subdomainService.createSubdomainForBusiness(businessId, preferredSubdomain);

            log.info("✅ Subdomain created successfully: {} -> {}",
                    preferredSubdomain, subdomainResponse.getFullDomain());

            return subdomainResponse;

        } catch (Exception e) {
            log.error("❌ Failed to create subdomain for business: {} - Error: {}", businessId, e.getMessage());

            // ✅ Create a fallback response instead of returning null
            SubdomainResponse fallbackResponse = new SubdomainResponse();
            fallbackResponse.setSubdomain("subdomain-creation-failed");
            fallbackResponse.setFullDomain("subdomain-creation-failed.menu.com");
            fallbackResponse.setFullUrl("https://subdomain-creation-failed.menu.com");
            fallbackResponse.setCanAccess(false);
            fallbackResponse.setNotes("Failed to create subdomain: " + e.getMessage());

            log.warn("⚠️ Returning fallback subdomain response due to creation failure");
            return fallbackResponse;
        }
    }


    // ================================
    // UTILITY METHODS
    // ================================

    @Transactional(readOnly = true)
    private boolean existsByUserIdentifier(String userIdentifier) {
        return userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier);
    }

    // ================================
    // PRIVATE HELPER METHODS
    // ================================

    @Transactional(readOnly = true)
    private boolean isBusinessNameTaken(String name) {
        return businessRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name);
    }

    private boolean isValidSimplePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }

        // Simple validation: 8-12 digits with optional spaces
        String cleanPhone = phone.replaceAll("\\s", ""); // Remove spaces
        return cleanPhone.matches("^[0-9]{8,12}$");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    private void validateAndAssignBusiness(User user, UUID businessId) {
        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new ValidationException("Business not found"));

        // ✅ Security check: Only platform users or business owners can assign users to businesses
        User currentUser = securityUtils.getCurrentUser();
        if (!currentUser.isPlatformUser() && !currentUser.getBusinessId().equals(businessId)) {
            throw new ValidationException("You can only assign users to your own business");
        }

        user.setBusinessId(businessId);
        log.debug("Assigned user to business: {}", business.getName());
    }

    private void setUserRoles(User user, List<RoleEnum> roleEnums) {
        if (roleEnums == null || roleEnums.isEmpty()) {
            throw new ValidationException("At least one role is required");
        }

        List<Role> roles = roleRepository.findByNameIn(roleEnums);
        if (roles.size() != roleEnums.size()) {
            throw new ValidationException("One or more roles not found");
        }

        user.setRoles(roles);
        log.debug("Assigned roles to user: {}", roleEnums);
    }

}