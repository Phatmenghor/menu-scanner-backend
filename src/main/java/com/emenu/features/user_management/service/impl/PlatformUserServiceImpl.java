package com.emenu.features.user_management.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.mapper.UserMapper;
import com.emenu.features.user_management.models.User;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.PlatformUserService;
import com.emenu.features.user_management.service.UserService;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformUserServiceImpl implements PlatformUserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createPlatformUser(UserCreateRequest request) {
        log.info("Creating platform user with email: {}", request.getEmail());

        // Validate platform user roles
        validatePlatformUserRequest(request);
        
        request.setUserType(UserType.PLATFORM_USER);
        return userService.createUser(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getAllPlatformUsers(UserFilterRequest filter) {
        filter.setUserType(UserType.PLATFORM_USER);
        return userService.getUsers(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getAllBusinessUsers(UserFilterRequest filter) {
        filter.setUserType(UserType.BUSINESS_USER);
        return userService.getUsers(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getAllCustomers(UserFilterRequest filter) {
        filter.setUserType(UserType.CUSTOMER);
        return userService.getUsers(filter);
    }

    @Override
    public UserResponse updatePlatformUser(UUID id, UserUpdateRequest request) {
        log.info("Updating platform user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        return userService.updateUser(id, request);
    }

    @Override
    public void deletePlatformUser(UUID id) {
        log.info("Deleting platform user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        userService.deleteUser(id);
    }

    @Override
    public void forcePasswordReset(UUID userId) {
        log.info("Force password reset for user ID: {}", userId);
        
        String tempPassword = "TempPass123!";
        userService.resetPassword(userId, tempPassword);
        
        log.info("Temporary password set for user ID: {}", userId);
    }

    @Override
    public void lockUser(UUID userId) {
        log.info("Locking user with ID: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setAccountStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        
        log.info("User locked successfully with ID: {}", userId);
    }

    @Override
    public void unlockUser(UUID userId) {
        log.info("Unlocking user with ID: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("User unlocked successfully with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getPlatformUsersCount() {
        return userRepository.countByUserType(UserType.PLATFORM_USER);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBusinessUsersCount() {
        return userRepository.countByUserType(UserType.BUSINESS_USER);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCustomersCount() {
        return userRepository.countByUserType(UserType.CUSTOMER);
    }

    private void validatePlatformUserRequest(UserCreateRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new ValidationException("Platform user must have at least one role");
        }

        List<RoleEnum> platformRoles = Arrays.asList(
                RoleEnum.PLATFORM_OWNER,
                RoleEnum.PLATFORM_ADMIN,
                RoleEnum.PLATFORM_SUPPORT
        );

        boolean hasValidRole = request.getRoles().stream()
                .anyMatch(platformRoles::contains);

        if (!hasValidRole) {
            throw new ValidationException("Invalid role for platform user");
        }
    }
}
