package com.emenu.security;

import com.emenu.exception.UserNotFoundException;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
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
        return userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));
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
}
