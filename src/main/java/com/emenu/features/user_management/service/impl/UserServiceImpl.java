package com.emenu.features.user_management.service.impl;

import com.emenu.enums.*;
import com.emenu.exception.*;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.ChangePasswordRequest;
import com.emenu.features.user_management.dto.request.CreateUserRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UpdateUserRequest;
import com.emenu.features.user_management.mapper.UserMapper;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.UserService;
import com.emenu.features.user_management.specification.UserSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import com.emenu.utils.validation.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
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
    private final HttpServletRequest request;

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(CreateUserRequest createRequest) {
        log.info("Creating new user with email: {}", createRequest.getEmail());

        // Validate request
        validateCreateUserRequest(createRequest);

        // Check if email already exists
        if (userRepository.existsByEmailAndIsDeletedFalse(createRequest.getEmail())) {
            throw new ValidationException("Email already exists: " + createRequest.getEmail());
        }

        // Check if phone already exists (if provided)
        if (createRequest.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumberAndIsDeletedFalse(createRequest.getPhoneNumber())) {
            throw new ValidationException("Phone number already exists: " + createRequest.getPhoneNumber());
        }

        // Check employee ID for platform users
        if (createRequest.getEmployeeId() != null &&
                userRepository.existsByEmployeeIdAndIsDeletedFalse(createRequest.getEmployeeId())) {
            throw new ValidationException("Employee ID already exists: " + createRequest.getEmployeeId());
        }

        // Map to entity
        User user = userMapper.toEntity(createRequest);
        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));

        // Set roles
        List<Role> roles = createRequest.getRoles().stream()
                .map(roleName -> roleRepository.findByNameAndIsDeletedFalse(roleName)
                        .orElseThrow(() -> new ValidationException("Role not found: " + roleName)))
                .toList();
        user.setRoles(roles);

        // Set registration IP
        user.setRegistrationIp(getClientIpAddress());

        // Generate tokens for verification
        generateVerificationTokens(user);

        // Set default subscription for business owners
        if (user.isBusinessUser() && user.hasRole(RoleEnum.BUSINESS_OWNER)) {
            setDefaultSubscription(user);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // Send verification emails asynchronously
        sendVerificationNotifications(savedUser);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Check access permissions
        validateUserAccess(user);

        return userMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(UUID id, UpdateUserRequest updateRequest) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Check access permissions
        validateUserAccess(user);

        // Validate email if changed
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            ValidationUtils.validateEmail(updateRequest.getEmail());
            if (userRepository.existsByEmailAndIsDeletedFalse(updateRequest.getEmail())) {
                throw new ValidationException("Email already exists: " + updateRequest.getEmail());
            }
            user.setEmailVerified(false); // Reset email verification
            generateEmailVerificationToken(user);
        }

        // Validate phone if changed
        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().equals(user.getPhoneNumber())) {
            ValidationUtils.validatePhone(updateRequest.getPhoneNumber());
            if (userRepository.existsByPhoneNumberAndIsDeletedFalse(updateRequest.getPhoneNumber())) {
                throw new ValidationException("Phone number already exists: " + updateRequest.getPhoneNumber());
            }
            user.setPhoneVerified(false); // Reset phone verification
            generatePhoneVerificationToken(user);
        }

        // Update entity
        userMapper.updateEntityFromRequest(updateRequest, user);

        // Update roles if provided and user has permission
        if (updateRequest.getRoles() != null && canUpdateRoles(user)) {
            List<Role> roles = updateRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByNameAndIsDeletedFalse(roleName)
                            .orElseThrow(() -> new ValidationException("Role not found: " + roleName)))
                    .toList();
            user.setRoles(roles);
        }

        // Update business access if provided
        if (updateRequest.getAccessibleBusinessIds() != null && canUpdateBusinessAccess(user)) {
            user.setAccessibleBusinessIds(updateRequest.getAccessibleBusinessIds());
        }

        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(UUID id) {
        log.info("Soft deleting user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Check permissions
        validateUserDeletion(user);

        user.softDelete();
        user.setDeletedBy(securityUtils.getCurrentUserEmail());
        userRepository.save(user);

        log.info("User soft deleted successfully with ID: {}", id);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void restoreUser(UUID id) {
        log.info("Restoring user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.getIsDeleted()) {
            throw new ValidationException("User is not deleted");
        }

        // Only platform admins can restore users
        if (!securityUtils.isPlatformAdmin()) {
            throw new InsufficientPermissionsException("Only platform administrators can restore users");
        }

        user.restore();
        userRepository.save(user);

        log.info("User restored successfully with ID: {}", id);
    }

    @Override
    public PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter) {
        log.info("Fetching users with filter: {}", filter);

        // Apply security filters based on current user's role
        applySecurityFilters(filter);

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
    @CacheEvict(value = "users", key = "#id")
    public UserResponse changePassword(UUID id, ChangePasswordRequest request) {
        log.info("Changing password for user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Check access permissions
        validateUserAccess(user);

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
        user.setLastPasswordChange(LocalDateTime.now());
        user.setPasswordChangeRequired(false);

        User savedUser = userRepository.save(user);

        // Send security notification
        sendPasswordChangeNotification(savedUser);

        log.info("Password changed successfully for user with ID: {}", id);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void lockUser(UUID id) {
        log.info("Locking user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Only platform admins can lock users
        if (!securityUtils.isPlatformAdmin()) {
            throw new InsufficientPermissionsException("Only platform administrators can lock users");
        }

        user.setAccountStatus(AccountStatus.LOCKED);
        user.setAccountLockedUntil(LocalDateTime.now().plusDays(30)); // Lock for 30 days
        userRepository.save(user);

        // Send notification
        sendAccountStatusNotification(user, "locked");

        log.info("User locked successfully with ID: {}", id);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void unlockUser(UUID id) {
        log.info("Unlocking user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Only platform admins can unlock users
        if (!securityUtils.isPlatformAdmin()) {
            throw new InsufficientPermissionsException("Only platform administrators can unlock users");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setAccountLockedUntil(null);
        user.setLoginAttempts(0);
        userRepository.save(user);

        // Send notification
        sendAccountStatusNotification(user, "unlocked");

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

        // Activate account if it was pending verification
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        // Send welcome notification
        sendWelcomeNotification(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void verifyPhone(String token) {
        log.info("Verifying phone with token: {}", token);

        User user = userRepository.findByPhoneVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid phone verification token"));

        if (user.getPhoneVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Phone verification token has expired");
        }

        user.setPhoneVerified(true);
        user.setPhoneVerificationToken(null);
        user.setPhoneVerificationExpires(null);

        userRepository.save(user);
        log.info("Phone verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void resetPassword(String email) {
        log.info("Initiating password reset for email: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        String resetToken = generatePasswordResetToken();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // Send password reset email
        sendPasswordResetNotification(user, resetToken);

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
        user.setLastPasswordChange(LocalDateTime.now());

        // Unlock account if it was locked
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        // Send confirmation notification
        sendPasswordChangeNotification(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    @Cacheable(value = "currentUser")
    public UserResponse getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        currentUser.updateActivity(); // Update last active time
        userRepository.save(currentUser);
        return userMapper.toResponse(currentUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void addLoyaltyPoints(UUID userId, int points) {
        log.info("Adding {} loyalty points to user: {}", points, userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Check permissions
        if (!canManageCustomer(user)) {
            throw new InsufficientPermissionsException("Insufficient permissions to manage customer loyalty points");
        }

        user.addLoyaltyPoints(points);
        userRepository.save(user);

        // Send loyalty points notification
        sendLoyaltyPointsNotification(user, points);

        log.info("Loyalty points added. User {} now has {} points and tier: {}",
                userId, user.getLoyaltyPoints(), user.getCustomerTier());
    }

    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void updateCustomerStats(UUID userId, double orderAmount) {
        log.info("Updating customer stats for user: {} with order amount: {}", userId, orderAmount);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.incrementTotalOrders();
        user.addToTotalSpent(orderAmount);

        // Calculate loyalty points based on tier multiplier
        CustomerTier currentTier = user.getCustomerTier();
        int loyaltyPoints = (int) Math.floor(orderAmount * currentTier.getPointMultiplier());
        user.addLoyaltyPoints(loyaltyPoints);

        userRepository.save(user);

        log.info("Customer stats updated for user: {}. Total orders: {}, Total spent: {}, Loyalty points: {}",
                userId, user.getTotalOrders(), user.getTotalSpent(), user.getLoyaltyPoints());
    }

    @Override
    public void grantBusinessAccess(UUID userId, UUID businessId) {
        log.info("Granting business access to user: {} for business: {}", userId, businessId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Check permissions
        if (!canManageBusinessAccess(businessId)) {
            throw new InsufficientPermissionsException("Insufficient permissions to manage business access");
        }

        user.grantBusinessAccess(businessId);
        userRepository.save(user);

        log.info("Business access granted to user: {} for business: {}", userId, businessId);
    }

    @Override
    public void revokeBusinessAccess(UUID userId, UUID businessId) {
        log.info("Revoking business access from user: {} for business: {}", userId, businessId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Check permissions
        if (!canManageBusinessAccess(businessId)) {
            throw new InsufficientPermissionsException("Insufficient permissions to manage business access");
        }

        user.revokeBusinessAccess(businessId);
        userRepository.save(user);

        log.info("Business access revoked from user: {} for business: {}", userId, businessId);
    }

    @Override
    public void updateSubscription(UUID userId, SubscriptionPlan plan, LocalDateTime endDate) {
        log.info("Updating subscription for user: {} to plan: {}", userId, plan);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Only platform admins or the user themselves can update subscription
        if (!securityUtils.isPlatformAdmin() && !securityUtils.isCurrentUser(userId)) {
            throw new InsufficientPermissionsException("Insufficient permissions to update subscription");
        }

        user.setSubscriptionPlan(plan);
        user.setSubscriptionEnds(endDate);
        if (user.getSubscriptionStarts() == null) {
            user.setSubscriptionStarts(LocalDateTime.now());
        }

        userRepository.save(user);

        // Send subscription update notification
        sendSubscriptionUpdateNotification(user);

        log.info("Subscription updated for user: {}", userId);
    }

    // Private helper methods
    private void validateCreateUserRequest(CreateUserRequest request) {
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());

        if (request.getPhoneNumber() != null) {
            ValidationUtils.validatePhone(request.getPhoneNumber());
        }

        // Validate business-specific requirements
        if (request.getUserType() == UserType.BUSINESS_USER) {
            if (request.getRoles().contains(RoleEnum.BUSINESS_OWNER) && request.getCompany() == null) {
                throw new ValidationException("Company name is required for business owners");
            }
        }

        // Validate platform-specific requirements
        if (request.getUserType() == UserType.PLATFORM_USER) {
            if (request.getEmployeeId() == null) {
                throw new ValidationException("Employee ID is required for platform users");
            }
            if (request.getDepartment() == null) {
                throw new ValidationException("Department is required for platform users");
            }
        }
    }

    private void validateUserAccess(User user) {
        // Platform admins can access all users
        if (securityUtils.isPlatformAdmin()) {
            return;
        }

        // Users can access their own profile
        if (securityUtils.isCurrentUser(user.getId())) {
            return;
        }

        // Business owners can access their staff
        if (securityUtils.isBusinessOwner() && user.getBusinessId() != null) {
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser.canAccessBusiness(user.getBusinessId())) {
                return;
            }
        }

        throw new InsufficientPermissionsException("Insufficient permissions to access this user");
    }

    private void validateUserDeletion(User user) {
        // Platform admins can delete most users
        if (securityUtils.isPlatformAdmin()) {
            // Cannot delete platform owners
            if (user.hasRole(RoleEnum.PLATFORM_OWNER)) {
                throw new InsufficientPermissionsException("Cannot delete platform owner");
            }
            return;
        }

        // Business owners can delete their staff (not other owners)
        if (securityUtils.isBusinessOwner()) {
            User currentUser = securityUtils.getCurrentUser();
            if (user.getBusinessId() != null && currentUser.canAccessBusiness(user.getBusinessId()) &&
                    !user.hasRole(RoleEnum.BUSINESS_OWNER)) {
                return;
            }
        }

        throw new InsufficientPermissionsException("Insufficient permissions to delete this user");
    }

    private boolean canUpdateRoles(User user) {
        // Platform admins can update most roles
        if (securityUtils.isPlatformAdmin()) {
            return true;
        }

        // Business owners can update roles within their business
        if (securityUtils.isBusinessOwner() && user.getBusinessId() != null) {
            User currentUser = securityUtils.getCurrentUser();
            return currentUser.canAccessBusiness(user.getBusinessId());
        }

        return false;
    }

    private boolean canUpdateBusinessAccess(User user) {
        return securityUtils.isPlatformAdmin() ||
                (securityUtils.isBusinessOwner() && user.getBusinessId() != null);
    }

    private boolean canManageCustomer(User customer) {
        if (securityUtils.isPlatformAdmin()) {
            return true;
        }

        // Business users can manage customers who have interacted with their business
        if (securityUtils.isBusinessUser()) {
            User currentUser = securityUtils.getCurrentUser();
            return customer.getAccessibleBusinessIds() != null &&
                    customer.getAccessibleBusinessIds().stream()
                            .anyMatch(businessId -> currentUser.canAccessBusiness(businessId));
        }

        return false;
    }

    private boolean canManageBusinessAccess(UUID businessId) {
        if (securityUtils.isPlatformAdmin()) {
            return true;
        }

        return securityUtils.canAccessBusiness(businessId);
    }

    private void applySecurityFilters(UserFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();

        // Platform users can see all users
        if (currentUser.isPlatformUser()) {
            return;
        }

        // Business users can only see users related to their businesses
        if (currentUser.isBusinessUser()) {
            if (filter.getBusinessId() == null && filter.getAccessibleBusinessIds() == null) {
                filter.setAccessibleBusinessIds(currentUser.getAccessibleBusinessIds());
            }
        }

        // Customers can only see themselves
        if (currentUser.isCustomer()) {
            // This should be handled at controller level, but adding as safety
            throw new InsufficientPermissionsException("Customers cannot list other users");
        }
    }

    private Specification<User> buildUserSpecification(UserFilterRequest filter) {
        return Specification.where(UserSpecification.isDeleted(filter.getIsDeleted()))
                .and(UserSpecification.hasSearch(filter.getSearch()))
                .and(UserSpecification.hasUserType(filter.getUserType()))
                .and(UserSpecification.hasAccountStatus(filter.getAccountStatus()))
                .and(filter.getHasAnyRole() != null && filter.getHasAnyRole() ?
                        UserSpecification.hasAnyRole(filter.getRoles()) :
                        UserSpecification.hasAllRoles(filter.getRoles()))
                .and(UserSpecification.hasCustomerTier(filter.getCustomerTier()))
                .and(UserSpecification.hasBusinessId(filter.getBusinessId()))
                .and(UserSpecification.hasPrimaryBusinessId(filter.getPrimaryBusinessId()))
                .and(UserSpecification.hasSubscriptionPlan(filter.getSubscriptionPlan()))
                .and(UserSpecification.hasActiveSubscription(filter.getHasActiveSubscription()))
                .and(UserSpecification.subscriptionExpiringSoon(filter.getSubscriptionExpiringSoon()))
                .and(UserSpecification.hasDepartment(filter.getDepartment()))
                .and(UserSpecification.hasEmployeeId(filter.getEmployeeId()))
                .and(UserSpecification.hiredBetween(filter.getHiredAfter(), filter.getHiredBefore()))
                .and(UserSpecification.isEmailVerified(filter.getEmailVerified()))
                .and(UserSpecification.isPhoneVerified(filter.getPhoneVerified()))
                .and(UserSpecification.isTwoFactorEnabled(filter.getTwoFactorEnabled()))
                .and(UserSpecification.lastLoginBetween(filter.getLastLoginAfter(), filter.getLastLoginBefore()))
                .and(UserSpecification.lastActiveBetween(filter.getLastActiveAfter(), filter.getLastActiveBefore()))
                .and(UserSpecification.createdBetween(filter.getCreatedAfter(), filter.getCreatedBefore()))
                .and(UserSpecification.loyaltyPointsBetween(filter.getMinLoyaltyPoints(), filter.getMaxLoyaltyPoints()))
                .and(UserSpecification.totalSpentBetween(filter.getMinTotalSpent(), filter.getMaxTotalSpent()))
                .and(UserSpecification.totalOrdersBetween(filter.getMinTotalOrders(), filter.getMaxTotalOrders()))
                .and(UserSpecification.hasCity(filter.getCity()))
                .and(UserSpecification.hasState(filter.getState()))
                .and(UserSpecification.hasCountry(filter.getCountry()))
                .and(UserSpecification.hasUtmSource(filter.getUtmSource()))
                .and(UserSpecification.hasUtmMedium(filter.getUtmMedium()))
                .and(UserSpecification.hasUtmCampaign(filter.getUtmCampaign()))
                .and(UserSpecification.hasReferralCode(filter.getReferralCode()))
                .and(UserSpecification.hasTermsAccepted(filter.getTermsAccepted()))
                .and(UserSpecification.hasPrivacyAccepted(filter.getPrivacyAccepted()))
                .and(UserSpecification.hasDataProcessingConsent(filter.getDataProcessingConsent()))
                .and(UserSpecification.hasMarketingConsent(filter.getMarketingConsent()))
                .and(UserSpecification.hasEmailNotifications(filter.getEmailNotifications()))
                .and(UserSpecification.hasTelegramNotifications(filter.getTelegramNotifications()))
                .and(UserSpecification.hasMarketingEmails(filter.getMarketingEmails()))
                .and(UserSpecification.isActive(filter.getIsActive()))
                .and(UserSpecification.isLocked(filter.getIsLocked()))
                .and(UserSpecification.sessionCountBetween(filter.getMinSessionCount(), null))
                .and(UserSpecification.totalLoginTimeGreaterThan(filter.getMinTotalLoginTime()));
    }

    private void generateVerificationTokens(User user) {
        // Email verification
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));

        // Phone verification if phone provided
        if (user.getPhoneNumber() != null) {
            user.setPhoneVerificationToken(generatePhoneVerificationCode());
            user.setPhoneVerificationExpires(LocalDateTime.now().plusMinutes(15));
        }
    }

    private void generateEmailVerificationToken(User user) {
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));
    }

    private void generatePhoneVerificationToken(User user) {
        user.setPhoneVerificationToken(generatePhoneVerificationCode());
        user.setPhoneVerificationExpires(LocalDateTime.now().plusMinutes(15));
    }

    private String generatePhoneVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    private void setDefaultSubscription(User user) {
        user.setSubscriptionPlan(SubscriptionPlan.FREE);
        user.setSubscriptionStarts(LocalDateTime.now());
        user.setSubscriptionEnds(LocalDateTime.now().plusDays(SubscriptionPlan.FREE.getDefaultDurationDays()));
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Async notification methods
    @Async
    private void sendVerificationNotifications(User user) {
        // TODO: Implement email verification notification
        log.info("Sending verification notifications to user: {}", user.getEmail());
    }

    @Async
    private void sendPasswordChangeNotification(User user) {
        // TODO: Implement password change notification
        log.info("Sending password change notification to user: {}", user.getEmail());
    }

    @Async
    private void sendAccountStatusNotification(User user, String status) {
        // TODO: Implement account status notification
        log.info("Sending account {} notification to user: {}", status, user.getEmail());
    }

    @Async
    private void sendWelcomeNotification(User user) {
        // TODO: Implement welcome notification
        log.info("Sending welcome notification to user: {}", user.getEmail());
    }

    @Async
    private void sendPasswordResetNotification(User user, String token) {
        // TODO: Implement password reset notification
        log.info("Sending password reset notification to user: {}", user.getEmail());
    }

    @Async
    private void sendLoyaltyPointsNotification(User user, int points) {
        // TODO: Implement loyalty points notification
        log.info("Sending loyalty points notification to user: {} for {} points", user.getEmail(), points);
    }

    @Async
    private void sendSubscriptionUpdateNotification(User user) {
        // TODO: Implement subscription update notification
        log.info("Sending subscription update notification to user: {}", user.getEmail());
    }
}