package com.emenu.utils.database;

import com.emenu.constants.ErrorMessages;
import com.emenu.exceptoins.error.NotFoundException;
import com.emenu.feature.auth.models.UserEntity;
import com.emenu.feature.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityUtils {
    @Autowired
    private UserRepository userRepository;

    public UserEntity getCurrentUser() {
        // Retrieve the Authentication object from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If no authentication exists (e.g., anonymous user), log error and throw exception
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User not authenticated. Authentication object is null or not authenticated.");
            throw new NotFoundException(ErrorMessages.USER_NOT_AUTHENTICATED);
        }

        // Get the username (email) from the authentication object
        String username = authentication.getName();
        log.info("Fetching user with email: {}", username);

        // Fetch the user from the repository
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", username);
                    return new NotFoundException(String.format(ErrorMessages.EMAIL_NOT_FOUND, username));
                });

        log.info("User with email {} successfully retrieved", username);
        return user;
    }

    // Get the user ID from the token (usually the principal)
    public Long getUserIdFromToken() {
        // Assuming the token stores the userId as the principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            String username = user.getUsername();
            // Retrieve the userId from the username or other logic
            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(ErrorMessages.EMAIL_NOT_FOUND));
            return currentUser.getId();
        } else {
            throw new NotFoundException("Authentication principal is not of expected type.");
        }
    }
}