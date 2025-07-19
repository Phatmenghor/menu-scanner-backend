package com.emenu.features.user_management.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.PasswordChangeRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.mapper.UserMapper;
import com.emenu.features.user_management.models.Role;
import com.emenu.features.user_management.models.User;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.UserService;
import com.emenu.features.user_management.specication.UserSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // Validate email uniqueness
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Validate phone uniqueness if provided
        if (request.getPhoneNumber() != null && 
            userRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            throw new ValidationException("Phone number already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Update roles if provided
        if (request.getRoles() != null) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);
        
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.softDelete();
        userRepository.save(user);
        
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter) {
        // Convert pageNo from 1-based to 0-based
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        filter.setPageNo(pageNo);
        
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), 
                filter.getPageSize(), 
                filter.getSortBy(), 
                filter.getSortDirection()
        );

        Specification<User> spec = UserSpecification.buildSpecification(filter);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserSummaryResponse> content = userMapper.toSummaryResponseList(userPage.getContent());

        return PaginationResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNo(userPage.getNumber() + 1) // Convert back to 1-based
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        log.info("Changing password for user ID: {}", userId);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }

    @Override
    public void resetPassword(UUID userId, String newPassword) {
        log.info("Resetting password for user ID: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password reset successfully for user ID: {}", userId);
    }

    @Override
    public void toggleUserStatus(UUID userId) {
        log.info("Toggling status for user ID: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        AccountStatus newStatus = user.getAccountStatus() == AccountStatus.ACTIVE
                ? AccountStatus.INACTIVE 
                : AccountStatus.ACTIVE;
        
        user.setAccountStatus(newStatus);
        userRepository.save(user);
        
        log.info("User status toggled to {} for user ID: {}", newStatus, userId);
    }
}