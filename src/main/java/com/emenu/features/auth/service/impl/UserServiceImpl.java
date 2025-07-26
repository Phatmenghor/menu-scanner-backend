package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.service.UserService;
import com.emenu.features.auth.specification.UserSpecification;
import com.emenu.features.subdomain.service.SubdomainService;
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

    // ================================
    // BASIC USER CRUD OPERATIONS
    // ================================

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {} with type: {}", request.getEmail(), request.getUserType());

        // Validate email uniqueness
        if (existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate phone uniqueness if provided
        if (request.getPhoneNumber() != null && existsByPhone(request.getPhoneNumber())) {
            throw new ValidationException("Phone number already exists");
        }

        try {
            // ✅ Create user entity
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhoneNumber(request.getPhoneNumber());
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
            log.info("User created successfully: {} with type: {}", savedUser.getEmail(), savedUser.getUserType());

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

        // ✅ Security: Business users can only see users from their business
        validateUserAccess(user);

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Security: Validate access
        validateUserAccess(user);

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

        log.info("User updated successfully: {}", updatedUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Security: Validate access
        validateUserAccess(user);

        // ✅ Prevent self-deletion
        User currentUser = securityUtils.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new ValidationException("You cannot delete your own account");
        }

        user.softDelete();
        user = userRepository.save(user);
        log.info("User deleted: {}", user.getEmail());
        
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
        log.info("Current user profile updated: {}", updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    // ================================
    // BUSINESS OWNER CREATION
    // ================================

    @Override
    public UserResponse createBusinessOwner(BusinessOwnerCreateRequest request) {
        log.info("Creating business owner with business: {} for email: {}",
                request.getBusinessName(), request.getOwnerEmail());

        // ✅ Security: Only platform users can create business owners
        User currentUser = securityUtils.getCurrentUser();
        if (!currentUser.isPlatformUser()) {
            throw new ValidationException("Only platform administrators can create business owners");
        }

        // ✅ ENHANCED: Check for duplicate email and phone early
        if (existsByEmail(request.getOwnerEmail())) {
            throw new ValidationException("Owner email already exists: " + request.getOwnerEmail());
        }

        if (request.getOwnerPhone() != null && existsByPhone(request.getOwnerPhone())) {
            throw new ValidationException("Owner phone number already exists: " + request.getOwnerPhone());
        }

        // ✅ ENHANCED: Check for duplicate business email early
        if (request.getBusinessEmail() != null && businessRepository.existsByEmailAndIsDeletedFalse(request.getBusinessEmail())) {
            throw new ValidationException("Business email already exists: " + request.getBusinessEmail());
        }

        try {
            // ✅ STEP 1: Create business first
            BusinessCreateRequest businessRequest = new BusinessCreateRequest();
            businessRequest.setName(request.getBusinessName());
            businessRequest.setEmail(request.getBusinessEmail());
            businessRequest.setPhone(request.getBusinessPhone());
            businessRequest.setAddress(request.getBusinessAddress());
            businessRequest.setDescription(request.getBusinessDescription());

            BusinessResponse businessResponse = businessService.createBusiness(businessRequest);
            log.info("Business created for business owner: {} with ID: {}",
                    businessResponse.getName(), businessResponse.getId());

            // ✅ STEP 2: Create business owner
            User user = new User();
            user.setEmail(request.getOwnerEmail());
            user.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
            user.setFirstName(request.getOwnerFirstName());
            user.setLastName(request.getOwnerLastName());
            user.setPhoneNumber(request.getOwnerPhone());
            user.setAddress(request.getOwnerAddress());
            user.setUserType(UserType.BUSINESS_USER);
            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setBusinessId(businessResponse.getId());
            user.setPosition("Owner");

            // Set business owner role
            Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                    .orElseThrow(() -> new ValidationException("Business owner role not found"));
            user.setRoles(List.of(businessOwnerRole));

            User savedUser = userRepository.save(user);
            log.info("Business owner created successfully: {} for business: {}",
                    savedUser.getEmail(), businessResponse.getName());

            // ✅ STEP 3: Create subdomain with exact input using admin method
            try {
                subdomainService.createExactSubdomainForBusiness(
                        businessResponse.getId(),
                        request.getPreferredSubdomain()
                );
                log.info("✅ Subdomain created successfully: {} for business: {}",
                        request.getPreferredSubdomain(), businessResponse.getName());
            } catch (Exception e) {
                log.warn("❌ Failed to create subdomain for business owner: {} - Error: {}",
                        savedUser.getEmail(), e.getMessage());
                // Don't fail the user creation if subdomain creation fails, but log it
            }

            // ✅ FIX: Load user with business relationship for proper response
            User userWithBusiness = userRepository.findById(savedUser.getId())
                    .orElse(savedUser);

            // ✅ FIX: Manually set business name since JPA lazy loading might not work
            UserResponse response = userMapper.toResponse(userWithBusiness);
            response.setBusinessName(businessResponse.getName()); // Ensure business name is set
            response.setBusinessId(businessResponse.getId());

            return response;

        } catch (ValidationException ve) {
            // Re-throw validation exceptions as-is
            throw ve;
        } catch (Exception e) {
            log.error("Failed to create business owner: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create business owner: " + e.getMessage(), e);
        }
    }

    // ================================
    // UTILITY METHODS
    // ================================

    @Transactional(readOnly = true)
    private boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Transactional(readOnly = true)
    private boolean existsByPhone(String phoneNumber) {
        return userRepository.existsByPhoneNumberAndIsDeletedFalse(phoneNumber);
    }

    // ================================
    // PRIVATE HELPER METHODS
    // ================================

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

        // ✅ Validate role assignment permissions
        validateRoleAssignment(user, roleEnums);

        user.setRoles(roles);
        log.debug("Assigned roles to user: {}", roleEnums);
    }

    private void validateRoleAssignment(User user, List<RoleEnum> roleEnums) {
        User currentUser = securityUtils.getCurrentUser();

        // ✅ Platform users can assign any role
        if (currentUser.isPlatformUser()) {
            return;
        }

        // ✅ Business users can only assign business roles
        if (currentUser.isBusinessUser()) {
            boolean hasNonBusinessRole = roleEnums.stream()
                    .anyMatch(role -> !role.isBusinessRole());
            
            if (hasNonBusinessRole) {
                throw new ValidationException("Business users can only assign business roles");
            }

            // ✅ Business users can only assign to their own business
            if (user.getBusinessId() != null && !user.getBusinessId().equals(currentUser.getBusinessId())) {
                throw new ValidationException("You can only assign roles to users in your business");
            }
        }
    }

    private void validateUserAccess(User user) {
        User currentUser = securityUtils.getCurrentUser();

        // ✅ Platform users can access any user
        if (currentUser.isPlatformUser()) {
            return;
        }

        // ✅ Business users can only access users from their business
        if (currentUser.isBusinessUser()) {
            if (user.getBusinessId() == null || !user.getBusinessId().equals(currentUser.getBusinessId())) {
                throw new ValidationException("You can only access users from your business");
            }
        }

        // ✅ Customers can only access their own profile
        if (currentUser.isCustomer() && !user.getId().equals(currentUser.getId())) {
            throw new ValidationException("You can only access your own profile");
        }
    }
}