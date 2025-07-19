package com.emenu.security;

import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();
        String username = authentication.getName();

        try {
            User currentUser = userRepository.findByEmailAndIsDeletedFalse(username).orElse(null);
            if (currentUser == null) {
                return false;
            }

            // Check if user has the specific permission
            return currentUser.getRoles().stream()
                    .anyMatch(role -> role.hasPermission(permissionString));

        } catch (Exception e) {
            log.error("Error evaluating permission {} for user {}", permissionString, username, e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();
        String username = authentication.getName();

        try {
            User currentUser = userRepository.findByEmailAndIsDeletedFalse(username).orElse(null);
            if (currentUser == null) {
                return false;
            }

            // Handle specific target types
            switch (targetType.toLowerCase()) {
                case "user":
                    return hasUserPermission(currentUser, (UUID) targetId, permissionString);
                case "business":
                    return hasBusinessPermission(currentUser, (UUID) targetId, permissionString);
                default:
                    return false;
            }

        } catch (Exception e) {
            log.error("Error evaluating permission {} for user {} on {} {}", 
                    permissionString, username, targetType, targetId, e);
            return false;
        }
    }

    private boolean hasUserPermission(User currentUser, UUID targetUserId, String permission) {
        // Platform admins can do anything
        if (currentUser.isPlatformUser()) {
            return true;
        }

        // Users can manage themselves
        if (currentUser.getId().equals(targetUserId)) {
            return permission.startsWith("user:read") || permission.startsWith("user:update");
        }

        // Business owners can manage their staff
        if (currentUser.isBusinessUser()) {
            User targetUser = userRepository.findByIdAndIsDeletedFalse(targetUserId).orElse(null);
            if (targetUser != null && currentUser.canAccessBusiness(targetUser.getBusinessId())) {
                return permission.startsWith("user:read") || permission.startsWith("user:update");
            }
        }

        return false;
    }

    private boolean hasBusinessPermission(User currentUser, UUID businessId, String permission) {
        // Platform admins can access all businesses
        if (currentUser.isPlatformUser()) {
            return true;
        }

        // Business users can access their own business
        if (currentUser.isBusinessUser() && currentUser.canAccessBusiness(businessId)) {
            return true;
        }

        return false;
    }
}