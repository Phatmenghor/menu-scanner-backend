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
        log.info("Fetching user with username: {}", username);
        
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
            if (hasRole("ROLE_SUPER_ADMIN") || hasRole("ROLE_PLATFORM_ADMIN")) {
                return true;
            }
            
            // Business owners/staff can only access their own business
            return businessId.equals(currentUser.getBusinessId());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPlatformAdmin() {
        return hasRole("ROLE_SUPER_ADMIN") || hasRole("ROLE_PLATFORM_ADMIN");
    }

    public boolean isBusinessOwner() {
        return hasRole("ROLE_BUSINESS_OWNER");
    }

    public boolean isBusinessStaff() {
        return hasRole("ROLE_BUSINESS_STAFF") || hasRole("ROLE_BUSINESS_MANAGER");
    }

    public boolean isCustomer() {
        return hasRole("ROLE_CUSTOMER");
    }
}