package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
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
    private final BusinessService businessService;
    private final SubdomainService subdomainService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;


    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating business user with business: {} for email: {}", request.getBusinessName(), request.getEmail());

        // Validate email uniqueness
        if (existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate phone uniqueness if provided
        if (request.getPhoneNumber() != null && existsByPhone(request.getPhoneNumber())) {
            throw new ValidationException("Phone number already exists");
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
            log.info("Business created for business user: {} with ID: {}", businessResponse.getName(), businessResponse.getId());

            // ✅ STEP 2: Create business user
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
            user.setUserType(UserType.BUSINESS_USER);
            user.setAccountStatus(request.getAccountStatus());
            user.setBusinessId(businessResponse.getId());

            // Set business owner role
            Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                    .orElseThrow(() -> new ValidationException("Business owner role not found"));
            user.setRoles(List.of(businessOwnerRole));

            User savedUser = userRepository.save(user);
            log.info("Business user created successfully: {} for business: {}", savedUser.getEmail(), businessResponse.getName());

            // ✅ STEP 3: Create subdomain with EXACT input (no formatting)
            try {
                subdomainService.createExactSubdomainForBusiness(businessResponse.getId(), request.getPreferredSubdomain());
                log.info("Subdomain created successfully: {} for business: {}", request.getPreferredSubdomain(), businessResponse.getName());
            } catch (Exception e) {
                log.warn("Failed to create subdomain for business user: {} - Error: {}", savedUser.getEmail(), e.getMessage());
                // Don't fail the user creation if subdomain creation fails
            }

            return userMapper.toResponse(savedUser);

        } catch (Exception e) {
            log.error("Failed to create business user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create business user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getAllUsers(UserFilterRequest request) {
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
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse deleteUser(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.softDelete();
        user = userRepository.save(user);
        log.info("User deleted: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Use dedicated mapper method for restricted current user profile updates
        userMapper.updateCurrentUserProfile(request, currentUser);

        User updatedUser = userRepository.save(currentUser);
        log.info("Current user profile updated: {}", updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    private boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Transactional(readOnly = true)
    private boolean existsByPhone(String phoneNumber) {
        return userRepository.existsByPhoneNumberAndIsDeletedFalse(phoneNumber);
    }
}