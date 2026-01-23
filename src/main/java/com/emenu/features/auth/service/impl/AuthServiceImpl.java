package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RefreshTokenRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.RefreshTokenResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.RefreshTokenService;
import com.emenu.features.auth.service.UserValidationService;
import com.emenu.security.SecurityUtils;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.security.jwt.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final UserValidationService userValidationService;

    /**
     * Authenticates a user and generates a JWT token with context-aware user lookup
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt: {} (userType: {}, businessId: {})",
                request.getUserIdentifier(), request.getUserType(), request.getBusinessId());

        try {
            // Find user with context-aware lookup
            User user = findUserWithContext(request);

            // Validate context matches (if provided)
            validateLoginContext(request, user);

            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserIdentifier(), request.getPassword())
            );

            // Validate account status
            securityUtils.validateAccountStatus(user);

            // Validate business subscription and status for business users
            if (user.isBusinessUser() && user.getBusinessId() != null) {
                Business business = businessRepository.findById(user.getBusinessId())
                        .orElseThrow(() -> new ValidationException("Business not found"));

                // Check if business is active
                if (!business.isActive()) {
                    log.warn("Login denied: Business is not active - {}", business.getStatus());
                    throw new ValidationException("Your business account is currently " + business.getStatus() + ". Please contact support.");
                }

                // Check if business has active subscription
                if (!business.hasActiveSubscription()) {
                    log.warn("Login denied: Business subscription is not active");
                    throw new ValidationException("Your business subscription has expired. Please renew your subscription to continue.");
                }
            }

            // Generate access token
            String accessToken = jwtGenerator.generateAccessToken(authentication);

            // Generate refresh token
            String ipAddress = getClientIpAddress();
            String deviceInfo = getDeviceInfo();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, ipAddress, deviceInfo);

            // Build login response
            LoginResponse response = userMapper.toLoginResponse(user, accessToken);
            response.setRefreshToken(refreshToken.getToken());

            // Add business subscription info for business users
            if (user.isBusinessUser() && user.getBusinessId() != null) {
                Business business = businessRepository.findById(user.getBusinessId()).orElse(null);
                if (business != null) {
                    response.setBusinessStatus(business.getStatus().toString());
                    response.setIsSubscriptionActive(business.hasActiveSubscription());
                }
            }

            log.info("Login successful: {} (type: {}, businessId: {})",
                    user.getUserIdentifier(), user.getUserType(), user.getBusinessId());
            return response;

        } catch (ValidationException e) {
            log.warn("Login failed: {} - Reason: {}", request.getUserIdentifier(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Login failed: {} - Error: {}", request.getUserIdentifier(), e.getMessage());
            throw new ValidationException("Invalid credentials");
        }
    }

    /**
     * Find user with context-aware lookup based on userType and businessId.
     * UserType is REQUIRED to disambiguate which user account to authenticate.
     */
    private User findUserWithContext(LoginRequest request) {
        String userIdentifier = request.getUserIdentifier();
        UserType userType = request.getUserType();
        UUID businessId = request.getBusinessId();

        // Validate userType is provided (should be caught by @NotNull, but double-check)
        if (userType == null) {
            throw new ValidationException(
                    "User type is required. Please specify whether you are logging in as CUSTOMER, PLATFORM_USER, or BUSINESS_USER."
            );
        }

        // Case 1: BUSINESS_USER - requires businessId
        if (userType == UserType.BUSINESS_USER) {
            if (businessId == null) {
                throw new ValidationException(
                        "Business ID is required for business user login. Please provide businessId in your login request."
                );
            }

            log.debug("Looking up business user: {} in business: {}", userIdentifier, businessId);
            return userRepository.findByUserIdentifierAndBusinessIdAndIsDeletedFalse(userIdentifier, businessId)
                    .orElseThrow(() -> new ValidationException(
                            "User '" + userIdentifier + "' not found in the specified business"
                    ));
        }

        // Case 2: CUSTOMER or PLATFORM_USER - global uniqueness per type
        log.debug("Looking up {} user: {}", userType, userIdentifier);
        return userRepository.findByUserIdentifierAndUserTypeAndIsDeletedFalse(userIdentifier, userType)
                .orElseThrow(() -> new ValidationException(
                        "User '" + userIdentifier + "' not found as " + userType.name().toLowerCase().replace("_", " ")
                ));
    }

    /**
     * Validate that the found user matches the requested context.
     * This is a safety check - the findUserWithContext method should already ensure correct user.
     */
    private void validateLoginContext(LoginRequest request, User user) {
        // Validate userType matches
        if (!request.getUserType().equals(user.getUserType())) {
            throw new ValidationException(
                    "User type mismatch. Expected: " + request.getUserType() +
                            ", Found: " + user.getUserType()
            );
        }

        // Validate businessId for business users
        if (request.getUserType() == UserType.BUSINESS_USER) {
            if (user.getBusinessId() == null) {
                throw new ValidationException("User is not associated with any business");
            }
            if (!request.getBusinessId().equals(user.getBusinessId())) {
                throw new ValidationException("User does not belong to the specified business");
            }
        }
    }

    /**
     * Registers a new customer user
     */
    @Override
    public UserResponse registerCustomer(RegisterRequest request) {
        log.info("Customer registration: {}", request.getUserIdentifier());

        // Validate username uniqueness for CUSTOMER type (global uniqueness among customers)
        userValidationService.validateUsernameUniqueness(
                request.getUserIdentifier(),
                UserType.CUSTOMER,
                null
        );

        User user = userMapper.toEntity(request);
        user.setUserType(UserType.CUSTOMER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);

        log.info("Customer registered: {}", savedUser.getUserIdentifier());
        return userMapper.toResponse(savedUser);
    }

    /**
     * Logs out a user by blacklisting their JWT token and revoking refresh token
     */
    @Override
    public void logout(String authorizationHeader) {
        log.info("Processing logout");
        String token = extractToken(authorizationHeader);

        if (token == null || !jwtGenerator.validateToken(token)) {
            throw new ValidationException("Invalid token");
        }

        String userIdentifier = jwtGenerator.getUsernameFromJWT(token);

        // Blacklist access token
        tokenBlacklistService.blacklistToken(token, userIdentifier, "LOGOUT");

        // Revoke all refresh tokens for the user
        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                .orElseThrow(() -> new ValidationException("User not found"));
        refreshTokenService.revokeAllUserTokens(user.getId(), "LOGOUT");

        log.info("Logout successful: {}", userIdentifier);
    }

    /**
     * Changes the password for the currently authenticated user
     */
    @Override
    public UserResponse changePassword(PasswordChangeRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(currentUser);

        // Blacklist all access tokens
        tokenBlacklistService.blacklistAllUserTokens(currentUser.getUserIdentifier(), "PASSWORD_CHANGE");

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(currentUser.getId(), "PASSWORD_CHANGE");

        log.info("Password changed: {}", currentUser.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Resets a user's password (admin function)
     */
    @Override
    public UserResponse adminResetPassword(AdminPasswordResetRequest request) {
        log.info("Admin password reset: {}", request.getUserId());

        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        // Blacklist all access tokens
        tokenBlacklistService.blacklistAllUserTokens(user.getUserIdentifier(), "ADMIN_PASSWORD_RESET");

        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user.getId(), "ADMIN_PASSWORD_RESET");

        log.info("Admin password reset: {}", user.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return null;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP address", e);
        }
        return "Unknown";
    }

    /**
     * Get device info from request
     */
    private String getDeviceInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get device info", e);
        }
        return "Unknown";
    }

    /**
     * Refresh access token using refresh token
     */
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing refresh token request");

        // Verify refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new ValidationException("Invalid or expired refresh token"));

        // Get user
        User user = userRepository.findByIdAndIsDeletedFalse(refreshToken.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Validate account status
        securityUtils.validateAccountStatus(user);

        // Validate business subscription and status for business users
        if (user.isBusinessUser() && user.getBusinessId() != null) {
            Business business = businessRepository.findById(user.getBusinessId())
                    .orElseThrow(() -> new ValidationException("Business not found"));

            if (!business.isActive()) {
                log.warn("Refresh denied: Business is not active - {}", business.getStatus());
                throw new ValidationException("Your business account is currently " + business.getStatus() + ". Please contact support.");
            }

            if (!business.hasActiveSubscription()) {
                log.warn("Refresh denied: Business subscription is not active");
                throw new ValidationException("Your business subscription has expired. Please renew your subscription to continue.");
            }
        }

        // Get user roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        // Generate new access token
        String newAccessToken = jwtGenerator.generateAccessTokenFromUsername(user.getUserIdentifier(), roles);

        // Optionally, generate a new refresh token (rotate refresh tokens for better security)
        String ipAddress = getClientIpAddress();
        String deviceInfo = getDeviceInfo();
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, ipAddress, deviceInfo);

        // Revoke old refresh token
        refreshTokenService.revokeRefreshToken(request.getRefreshToken(), "TOKEN_REFRESH");

        log.info("Token refresh successful: {}", user.getUserIdentifier());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken.getToken());
    }
}