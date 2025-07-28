package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.*;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.mapper.AuthMapper;
import com.emenu.features.auth.mapper.RegistrationMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.SocialUserAccount;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.SocialUserAccountRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.security.SecurityUtils;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SocialUserAccountRepository socialUserAccountRepository;
    private final AuthMapper authMapper;
    private final RegistrationMapper registrationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for userIdentifier: {}", request.getUserIdentifier());

        // ✅ UPDATED: Use userIdentifier for authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserIdentifier(), request.getPassword())
        );

        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(request.getUserIdentifier())
                .orElseThrow(() -> new ValidationException("User not found"));

        securityUtils.validateAccountStatus(user);
        String token = jwtGenerator.generateAccessToken(authentication);
        LoginResponse response = authMapper.toLoginResponse(user, token);

        log.info("Login successful for user: {}", user.getUserIdentifier());
        return response;
    }

    @Override
    public void logout(String authorizationHeader) {
        log.info("Processing logout request");
        String token = extractToken(authorizationHeader);

        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Invalid token format. Authorization header must contain 'Bearer <token>'");
        }

        if (!jwtGenerator.validateToken(token)) {
            throw new ValidationException("Token is invalid or expired");
        }

        String userIdentifier;
        try {
            userIdentifier = jwtGenerator.getUsernameFromJWT(token);
            log.info("Processing logout for user: {}", userIdentifier);
        } catch (Exception e) {
            log.error("Failed to extract userIdentifier from token during logout: {}", e.getMessage());
            throw new ValidationException("Invalid token - cannot extract user information");
        }

        tokenBlacklistService.blacklistToken(token, userIdentifier, "LOGOUT");
        log.info("Logout successful for user: {} - token blacklisted", userIdentifier);
    }

    @Override
    public UserResponse registerCustomer(RegisterRequest request) {
        log.info("Registering new customer: {}", request.getUserIdentifier());

        validateCustomerRegistration(request);

        // Create customer user
        User user = registrationMapper.toCustomerEntity(request);
        user.setUserIdentifier(request.getUserIdentifier());
        user.setEmail(request.getEmail()); // Optional - can be null
        user.setPhoneNumber(request.getPhoneNumber()); // Optional - can be null
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ✅ NEW: Mark as regular account (not social-only)
        user.setHasPassword(true);
        user.setSocialOnlyAccount(false);

        // Set customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);
        log.info("Customer registered successfully: {}", savedUser.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse changePassword(PasswordChangeRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // ✅ NEW: Handle social-only accounts
        if (currentUser.isSocialOnlyAccount() || !currentUser.canLoginWithPassword()) {
            // User is setting password for the first time
            if (request.getCurrentPassword() != null && !request.getCurrentPassword().trim().isEmpty()) {
                throw new ValidationException("Current password should be empty for social-only accounts");
            }
        } else {
            // Verify current password for regular accounts
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                throw new ValidationException("Current password is incorrect");
            }
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        // ✅ NEW: Enable password login
        currentUser.enablePasswordLogin(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(currentUser);

        tokenBlacklistService.blacklistAllUserTokens(currentUser.getUserIdentifier(), "PASSWORD_CHANGE");
        log.info("Password {} for user: {}", 
                currentUser.isSocialOnlyAccount() ? "set" : "changed", 
                currentUser.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse adminResetPassword(AdminPasswordResetRequest request) {
        log.info("Admin password reset for user: {}", request.getUserId());

        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        // ✅ NEW: Enable password login for the user
        user.enablePasswordLogin(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        tokenBlacklistService.blacklistAllUserTokens(user.getUserIdentifier(), "ADMIN_PASSWORD_RESET");
        log.info("Admin password reset successful for user: {} by admin", user.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    // ✅ NEW: Check if user can login with different methods
    @Transactional(readOnly = true)
    public LoginMethodsResponse getLoginMethods(String userIdentifier) {
        log.info("Checking login methods for user: {}", userIdentifier);

        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                .orElseThrow(() -> new ValidationException("User not found"));

        boolean canLoginWithPassword = user.canLoginWithPassword();
        boolean canLoginSocially = user.canLoginSocially();
        
        List<String> availableProviders = List.of();
        if (canLoginSocially) {
            availableProviders = socialUserAccountRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .stream()
                    .map(account -> account.getProvider().name())
                    .distinct()
                    .toList();
        }

        return LoginMethodsResponse.builder()
                .userIdentifier(userIdentifier)
                .canLoginWithPassword(canLoginWithPassword)
                .canLoginSocially(canLoginSocially)
                .availableSocialProviders(availableProviders)
                .accountStatus(user.getAccountStatus().name())
                .userType(user.getUserType().name())
                .build();
    }

    // ✅ NEW: Update user profile with social account data
    @Transactional
    public void syncUserProfileWithSocialAccount(User user, SocialUserAccount socialAccount) {
        log.info("Syncing user profile with social account: {} for user: {}", 
                socialAccount.getProvider(), user.getUserIdentifier());

        boolean updated = false;

        // Update profile picture if not set
        if (user.getProfileImageUrl() == null && socialAccount.getProviderPictureUrl() != null) {
            user.setProfileImageUrl(socialAccount.getProviderPictureUrl());
            updated = true;
        }

        // Update name if not set
        if (user.getFirstName() == null && socialAccount.getProviderName() != null) {
            String[] nameParts = socialAccount.getProviderName().trim().split("\\s+", 2);
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            }
            updated = true;
        }

        // Update email if not set (for Google accounts)
        if (user.getEmail() == null && socialAccount.getProviderEmail() != null) {
            user.setEmail(socialAccount.getProviderEmail());
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            log.info("User profile updated with social account data for: {}", user.getUserIdentifier());
        }
    }

    // Helper methods
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return null;
    }

    @Transactional(readOnly = true)
    private boolean isUserIdentifierAvailable(String userIdentifier) {
        return !userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier);
    }

    private void validateCustomerRegistration(RegisterRequest request) {
        // ✅ UPDATED: Only check userIdentifier uniqueness
        if (!isUserIdentifierAvailable(request.getUserIdentifier())) {
            throw new ValidationException("User identifier is already in use");
        }

        // ✅ REMOVED: No email/phone uniqueness checks for regular customers
        // Email and phone are now optional and don't need to be unique
    }

    // ✅ NEW: Response DTO for login methods
    @lombok.Data
    @lombok.Builder
    public static class LoginMethodsResponse {
        private String userIdentifier;
        private boolean canLoginWithPassword;
        private boolean canLoginSocially;
        private List<String> availableSocialProviders;
        private String accountStatus;
        private String userType;
    }
}