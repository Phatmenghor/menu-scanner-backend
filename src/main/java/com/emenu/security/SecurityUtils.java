package com.emenu.security;

import com.emenu.enums.user.AccountStatus;
import com.emenu.exception.custom.AccountInactiveException;
import com.emenu.exception.custom.AccountSuspendedException;
import com.emenu.exception.custom.UserNotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountLockedException;
import java.util.UUID;

@Component
@Slf4j
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User not authenticated");
        }

        String userIdentifier = authentication.getName();

        // Find user by userIdentifier (supports both traditional and Telegram users)
        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                .orElseThrow(() -> {
                    log.error("❌ User not found with userIdentifier: {}", userIdentifier);
                    return new UserNotFoundException("User not found with userIdentifier: " + userIdentifier);
                });

        // Log user access for audit trail
        log.debug("🔐 Current user access: {} ({}), Type: {}, Telegram: {}",
                user.getDisplayName(),
                user.getUserIdentifier(),
                user.getUserType(),
                user.hasTelegramLinked() ? "Linked" : "Not Linked");

        // Double-check account status for security
        validateAccountStatus(user);

        // Update Telegram activity if user has Telegram linked
        if (user.hasTelegramLinked()) {
            user.updateTelegramActivity();
            userRepository.save(user);
        }

        return user;
    }

    public void validateAccountStatus(User user) {
        switch (user.getAccountStatus()) {
            case INACTIVE -> {
                log.warn("🚫 Access attempt by inactive user: {} ({})",
                        user.getDisplayName(), user.getUserIdentifier());
                throw new AccountInactiveException("Account is inactive. Please contact support.");
            }
            case LOCKED -> {
                log.warn("🔒 Access attempt by locked user: {} ({})",
                        user.getDisplayName(), user.getUserIdentifier());
                try {
                    throw new AccountLockedException("Account is locked due to security reasons. Please contact support.");
                } catch (AccountLockedException e) {
                    throw new RuntimeException(e);
                }
            }
            case SUSPENDED -> {
                log.warn("⏸️ Access attempt by suspended user: {} ({})",
                        user.getDisplayName(), user.getUserIdentifier());
                throw new AccountSuspendedException("Account is suspended. Please contact support for reactivation.");
            }
            case ACTIVE -> {
                // All good, continue
                log.debug("✅ Account status valid for user: {}", user.getUserIdentifier());
            }
            default -> {
                log.error("❓ Unknown account status for user: {} - Status: {}",
                        user.getUserIdentifier(), user.getAccountStatus());
                throw new AccountInactiveException("Account status is unknown. Please contact support.");
            }
        }
    }

    // ===== USER IDENTIFICATION METHODS =====

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserIdentifier() {
        return getCurrentUser().getUserIdentifier();
    }

    public String getCurrentUserEmail() {
        User user = getCurrentUser();
        return user.getEmail(); // Can be null for Telegram-only users
    }

    public String getCurrentUserDisplayName() {
        return getCurrentUser().getDisplayName();
    }

    // ===== TELEGRAM-SPECIFIC METHODS =====

    public boolean currentUserHasTelegram() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.hasTelegramLinked();
        } catch (Exception e) {
            log.debug("Error checking Telegram status: {}", e.getMessage());
            return false;
        }
    }

    public Long getCurrentUserTelegramId() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getTelegramUserId();
        } catch (Exception e) {
            log.debug("Error getting Telegram ID: {}", e.getMessage());
            return null;
        }
    }

    public boolean currentUserCanReceiveTelegramNotifications() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.canReceiveTelegramNotifications();
        } catch (Exception e) {
            log.debug("Error checking Telegram notifications: {}", e.getMessage());
            return false;
        }
    }

    public String getCurrentUserAuthenticationMethod() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getSocialProvider().isTelegram()) {
                return "Telegram";
            } else if (currentUser.getSocialProvider().isLocal()) {
                return currentUser.hasTelegramLinked() ? "Traditional (Telegram Linked)" : "Traditional";
            } else {
                return currentUser.getSocialProvider().getDisplayName();
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ===== BUSINESS ACCESS METHODS =====

    public boolean hasBusinessAccess(UUID businessId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getBusinessId() != null &&
                    currentUser.getBusinessId().equals(businessId);
        } catch (Exception e) {
            log.debug("Error checking business access: {}", e.getMessage());
            return false;
        }
    }

    public UUID getCurrentUserBusinessId() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getBusinessId();
        } catch (Exception e) {
            log.debug("Error getting business ID: {}", e.getMessage());
            return null;
        }
    }

    // ===== USER TYPE METHODS =====

    public boolean isPlatformUser() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isPlatformUser();
        } catch (Exception e) {
            log.debug("Error checking platform user status: {}", e.getMessage());
            return false;
        }
    }

    public boolean isBusinessUser() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isBusinessUser();
        } catch (Exception e) {
            log.debug("Error checking business user status: {}", e.getMessage());
            return false;
        }
    }

    public boolean isCustomer() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isCustomer();
        } catch (Exception e) {
            log.debug("Error checking customer status: {}", e.getMessage());
            return false;
        }
    }

    // ===== VALIDATION METHODS =====

    public boolean canUserLogin(User user) {
        return user != null &&
                !user.getIsDeleted() &&
                AccountStatus.ACTIVE.equals(user.getAccountStatus());
    }

    public boolean canUserLoginWithPassword(User user) {
        return canUserLogin(user) &&
                (user.getPassword() != null || user.getSocialProvider().requiresPassword());
    }

    public boolean canUserLoginWithTelegram(User user) {
        return canUserLogin(user) &&
                user.hasTelegramLinked() &&
                user.getSocialProvider().isSocial();
    }

    // ===== ACCOUNT STATUS HELPERS =====

    public String getAccountStatusMessage(AccountStatus status) {
        return switch (status) {
            case ACTIVE -> "Account is active and ready to use";
            case INACTIVE -> "Account is inactive. Please contact support to reactivate your account.";
            case LOCKED -> "Account is locked due to security reasons. Please contact support to unlock your account.";
            case SUSPENDED -> "Account is suspended. Please contact support for account reactivation.";
        };
    }

    public String getCurrentUserSummary() {
        try {
            User user = getCurrentUser();
            return String.format("User: %s (%s), Type: %s, Status: %s, Provider: %s, Telegram: %s",
                    user.getDisplayName(),
                    user.getUserIdentifier(),
                    user.getUserType().getDescription(),
                    user.getAccountStatus().getDescription(),
                    user.getSocialProvider().getDisplayName(),
                    user.hasTelegramLinked() ? "Linked" : "Not Linked");
        } catch (Exception e) {
            return "Error getting user summary: " + e.getMessage();
        }
    }

    // ===== AUTHORIZATION HELPERS =====

    public boolean canManageUsers() {
        return isPlatformUser();
    }

    public boolean canManageBusinesses() {
        return isPlatformUser();
    }

    public boolean canManageOwnBusiness() {
        return isBusinessUser() && getCurrentUserBusinessId() != null;
    }

    public boolean canAccessTelegramFeatures() {
        return currentUserHasTelegram();
    }

    public boolean canReceiveNotifications() {
        return currentUserCanReceiveTelegramNotifications();
    }

    // ===== SECURITY AUDIT HELPERS =====

    public void logSecurityEvent(String event, String details) {
        try {
            User user = getCurrentUser();
            log.info("🔐 Security Event - User: {} ({}), Event: {}, Details: {}",
                    user.getDisplayName(),
                    user.getUserIdentifier(),
                    event,
                    details);
        } catch (Exception e) {
            log.info("🔐 Security Event - Anonymous User, Event: {}, Details: {}", event, details);
        }
    }

    public void logTelegramActivity(String activity) {
        try {
            User user = getCurrentUser();
            if (user.hasTelegramLinked()) {
                log.info("📱 Telegram Activity - User: {} ({}), Activity: {}",
                        user.getDisplayName(),
                        user.getUserIdentifier(),
                        activity);
            }
        } catch (Exception e) {
            log.debug("Error logging Telegram activity: {}", e.getMessage());
        }
    }
}