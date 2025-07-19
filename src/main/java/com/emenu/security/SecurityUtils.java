package com.emenu.security;

import com.emenu.exception.UserNotFoundException;
import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
        log.debug("Fetching user with username: {}", username);

        return userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    public boolean isCurrentUser(UUID userId) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCurrentUserEmail(String email) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getEmail().equals(email);
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

    public boolean canAccessBusiness(UUID businessId) {
        try {
            User currentUser = getCurrentUser();

            // Platform admins can access all businesses
            if (isPlatformAdmin()) {
                return true;
            }

            // Business users can access their own business
            return currentUser.canAccessBusiness(businessId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPlatformAdmin() {
        return hasRole("ROLE_PLATFORM_OWNER") || hasRole("ROLE_PLATFORM_MANAGER");
    }

    public boolean isPlatformUser() {
        return hasRole("ROLE_PLATFORM_OWNER") ||
                hasRole("ROLE_PLATFORM_MANAGER") ||
                hasRole("ROLE_PLATFORM_STAFF") ||
                hasRole("ROLE_PLATFORM_DEVELOPER") ||
                hasRole("ROLE_PLATFORM_SUPPORT") ||
                hasRole("ROLE_PLATFORM_SALES");
    }

    public boolean isBusinessOwner() {
        return hasRole("ROLE_BUSINESS_OWNER");
    }

    public boolean isBusinessStaff() {
        return hasRole("ROLE_BUSINESS_STAFF") || hasRole("ROLE_BUSINESS_MANAGER");
    }

    public boolean isBusinessUser() {
        return hasRole("ROLE_BUSINESS_OWNER") ||
                hasRole("ROLE_BUSINESS_MANAGER") ||
                hasRole("ROLE_BUSINESS_STAFF");
    }

    public boolean isCustomer() {
        return hasRole("ROLE_CUSTOMER") ||
                hasRole("ROLE_VIP_CUSTOMER") ||
                hasRole("ROLE_GUEST_CUSTOMER");
    }
}