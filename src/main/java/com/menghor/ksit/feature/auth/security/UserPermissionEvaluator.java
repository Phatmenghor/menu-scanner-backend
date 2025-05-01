package com.menghor.ksit.feature.auth.security;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Custom permission evaluator for user-related operations
 * Used in PreAuthorize annotations for fine-grained access control
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserPermissionEvaluator {

    private final UserRepository userRepository;

    /**
     * Check if the authenticated user is the same as the requested user ID
     */
    public boolean isSameUser(Authentication authentication, Long userId) {
        String username = extractUsername(authentication);
        if (username == null) return false;
        
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }

    /**
     * Check if a staff member can access a specific user
     * Staff can access students and themselves, but not other staff or admins
     */
    public boolean canAccessUser(Authentication authentication, Long userId) {
        String username = extractUsername(authentication);
        if (username == null) return false;
        
        // First check if it's the same user (staff accessing themselves)
        if (isSameUser(authentication, userId)) {
            return true;
        }
        
        // Get the target user
        Optional<UserEntity> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            return false;
        }
        
        UserEntity targetUser = targetUserOpt.get();
        
        // Staff can only access STUDENT users
        return targetUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);
    }

    /**
     * Check if a user can modify another user
     * Staff can modify students, but not other staff or admins
     * Everyone can modify limited information about themselves
     */
    public boolean canModifyUser(Authentication authentication, Long userId) {
        // Same rules as for access
        return canAccessUser(authentication, userId);
    }

    /**
     * Check if a student can update their own information
     * Students can only update certain fields about themselves
     */
    public boolean canStudentUpdateSelf(Authentication authentication, Long userId) {
        return isSameUser(authentication, userId);
    }

    /**
     * Helper method to extract username from authentication
     */
    private String extractUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
}