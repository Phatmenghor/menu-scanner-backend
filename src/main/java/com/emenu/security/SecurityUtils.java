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

        String username = authentication.getName();
        User user = userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));

        // Double-check account status for security
        validateAccountStatus(user);

        return user;
    }

    public void validateAccountStatus(User user) {
        switch (user.getAccountStatus()) {
            case INACTIVE -> {
                log.warn("Access attempt by inactive user: {}", user.getEmail());
                throw new AccountInactiveException("Account is inactive. Please contact support.");
            }
            case LOCKED -> {
                log.warn("Access attempt by locked user: {}", user.getEmail());
                try {
                    throw new AccountLockedException("Account is locked due to security reasons. Please contact support.");
                } catch (AccountLockedException e) {
                    throw new RuntimeException(e);
                }
            }
            case SUSPENDED -> {
                log.warn("Access attempt by suspended user: {}", user.getEmail());
                throw new AccountSuspendedException("Account is suspended. Please contact support for reactivation.");
            }
            case ACTIVE -> {
                // All good, continue
            }
            default -> {
                log.error("Unknown account status for user: {} - Status: {}", user.getEmail(), user.getAccountStatus());
                throw new AccountInactiveException("Account status is unknown. Please contact support.");
            }
        }
    }

    public boolean isCurrentUser(UUID userId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public boolean hasBusinessAccess(UUID businessId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getBusinessId() != null &&
                    currentUser.getBusinessId().equals(businessId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPlatformUser() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isPlatformUser();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBusinessUser() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isBusinessUser();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCustomer() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isCustomer();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canUserLogin(User user) {
        return user != null &&
                !user.getIsDeleted() &&
                AccountStatus.ACTIVE.equals(user.getAccountStatus());
    }

    public String getAccountStatusMessage(AccountStatus status) {
        return switch (status) {
            case ACTIVE -> "Account is active and ready to use";
            case INACTIVE -> "Account is inactive. Please contact support to reactivate your account.";
            case LOCKED -> "Account is locked due to security reasons. Please contact support to unlock your account.";
            case SUSPENDED -> "Account is suspended. Please contact support for account reactivation.";
        };
    }
}