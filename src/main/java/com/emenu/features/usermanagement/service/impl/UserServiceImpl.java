package com.emenu.features.usermanagement.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.dto.filter.UserFilterRequest;
import com.emenu.features.usermanagement.dto.request.ChangePasswordRequest;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import com.emenu.features.usermanagement.mapper.UserMapper;
import com.emenu.features.usermanagement.repository.RoleRepository;
import com.emenu.features.usermanagement.repository.UserRepository;
import com.emenu.features.usermanagement.service.UserService;
import com.emenu.features.usermanagement.specification.UserSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import com.emenu.utils.validation.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final SecurityUtils securityUtils;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Validate request
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());
        
        // Check if email already exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists: " + request.getEmail());
        }
        
        // Map to entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set roles
        List<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ValidationException("Role not found: " + roleName)))
                .toList();
        user.setRoles(roles);
        
        // Generate email verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional()
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Validate email if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            ValidationUtils.validateEmail(request.getEmail());
            if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
                throw new ValidationException("Email already exists: " + request.getEmail());
            }
            user.setEmailVerified(false); // Reset email verification if email changed
        }
        
        // Update entity
        userMapper.updateEntityFromRequest(request, user);
        
        // Update roles if provided
        if (request.getRoles() != null) {
            List<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ValidationException("Role not found: " + roleName)))
                    .toList();
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Soft deleting user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.softDelete();
        userRepository.save(user);
        
        log.info("User soft deleted successfully with ID: {}", id);
    }

    @Override
    public void restoreUser(UUID id) {
        log.info("Restoring user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        if (!user.getIsDeleted()) {
            throw new ValidationException("User is not deleted");
        }
        
        user.restore();
        userRepository.save(user);
        
        log.info("User restored successfully with ID: {}", id);
    }

    @Override
    @Transactional()
    public PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter) {
        log.info("Fetching users with filter: {}", filter);
        
        Specification<User> spec = buildUserSpecification(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), 
                filter.getPageSize(), 
                filter.getSortBy(), 
                filter.getSortDirection()
        );
        
        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserSummaryResponse> content = userMapper.toSummaryResponseList(userPage.getContent());
        
        return PaginationResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNo(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public UserResponse changePassword(UUID id, ChangePasswordRequest request) {
        log.info("Changing password for user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Validate new password
        ValidationUtils.validatePassword(request.getNewPassword());
        
        // Check password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);
        
        log.info("Password changed successfully for user with ID: {}", id);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void lockUser(UUID id) {
        log.info("Locking user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.setAccountStatus(AccountStatus.LOCKED);
        user.setAccountLockedUntil(LocalDateTime.now().plusDays(30)); // Lock for 30 days
        userRepository.save(user);
        
        log.info("User locked successfully with ID: {}", id);
    }

    @Override
    public void unlockUser(UUID id) {
        log.info("Unlocking user with ID: {}", id);
        
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setAccountLockedUntil(null);
        user.setLoginAttempts(0);
        userRepository.save(user);
        
        log.info("User unlocked successfully with ID: {}", id);
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);
        
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid email verification token"));
        
        if (user.getEmailVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Email verification token has expired");
        }
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);
        user.setAccountStatus(AccountStatus.ACTIVE);
        
        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void resetPassword(String email) {
        log.info("Initiating password reset for email: {}", email);
        
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        
        // TODO: Send password reset email
        log.info("Password reset token generated for user: {}", email);
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        log.info("Resetting password with token: {}", token);
        
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid password reset token"));
        
        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Password reset token has expired");
        }
        
        ValidationUtils.validatePassword(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional()
    public UserResponse getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public void addLoyaltyPoints(UUID userId, int points) {
        log.info("Adding {} loyalty points to user: {}", points, userId);
        
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.addLoyaltyPoints(points);
        userRepository.save(user);
        
        log.info("Loyalty points added. User {} now has {} points and tier: {}", 
                userId, user.getLoyaltyPoints(), user.getCustomerTier());
    }

    @Override
    public void updateCustomerStats(UUID userId, double orderAmount) {
        log.info("Updating customer stats for user: {} with order amount: {}", userId, orderAmount);
        
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.incrementTotalOrders();
        user.addToTotalSpent(orderAmount);
        
        // Calculate loyalty points (1 point per dollar spent)
        int loyaltyPoints = (int) Math.floor(orderAmount);
        user.addLoyaltyPoints(loyaltyPoints);
        
        userRepository.save(user);
        
        log.info("Customer stats updated for user: {}. Total orders: {}, Total spent: {}, Loyalty points: {}", 
                userId, user.getTotalOrders(), user.getTotalSpent(), user.getLoyaltyPoints());
    }

    private Specification<User> buildUserSpecification(UserFilterRequest filter) {
        return Specification.where(UserSpecification.notDeleted())
                .and(UserSpecification.hasSearch(filter.getSearch()))
                .and(UserSpecification.hasUserType(filter.getUserType()))
                .and(UserSpecification.hasAccountStatus(filter.getAccountStatus()))
                .and(UserSpecification.hasAnyRole(filter.getRoles()))
                .and(UserSpecification.hasCustomerTier(filter.getCustomerTier()))
                .and(UserSpecification.hasBusinessId(filter.getBusinessId()))
                .and(UserSpecification.isEmailVerified(filter.getEmailVerified()))
                .and(UserSpecification.isTwoFactorEnabled(filter.getTwoFactorEnabled()))
                .and(UserSpecification.createdAfter(filter.getCreatedAfter()))
                .and(UserSpecification.createdBefore(filter.getCreatedBefore()))
                .and(UserSpecification.lastLoginAfter(filter.getLastLoginAfter()))
                .and(UserSpecification.lastLoginBefore(filter.getLastLoginBefore()))
                .and(UserSpecification.loyaltyPointsBetween(filter.getMinLoyaltyPoints(), filter.getMaxLoyaltyPoints()))
                .and(UserSpecification.totalSpentBetween(filter.getMinTotalSpent(), filter.getMaxTotalSpent()));
    }
}