package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.mapper.AuthMapper;
import com.emenu.features.auth.mapper.RegistrationMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
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
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Get user details
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Additional security validation
        securityUtils.validateAccountStatus(user);

        // Generate JWT token
        String token = jwtGenerator.generateAccessToken(authentication);

        // Create response
        LoginResponse response = authMapper.toLoginResponse(user, token);

        log.info("Login successful for user: {}", user.getEmail());
        return response;
    }

    @Override
    public void logout(String authorizationHeader) {
        log.info("Processing logout request");

        // Extract and validate token
        String token = extractToken(authorizationHeader);

        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Invalid token format. Authorization header must contain 'Bearer <token>'");
        }

        // Validate token before blacklisting
        if (!jwtGenerator.validateToken(token)) {
            throw new ValidationException("Token is invalid or expired");
        }

        // Get user email from token
        String userEmail;
        try {
            userEmail = jwtGenerator.getUsernameFromJWT(token);
            log.info("Processing logout for user: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to extract username from token during logout: {}", e.getMessage());
            throw new ValidationException("Invalid token - cannot extract user information");
        }

        // Add token to blacklist with 1 week expiry
        tokenBlacklistService.blacklistToken(token, userEmail, "LOGOUT");

        log.info("Logout successful for user: {} - token blacklisted", userEmail);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new {} user: {}", request.getUserType(), request.getEmail());

        validateRegistration(request);

        // Create user entity based on type
        User user = createUserFromRequest(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        // Assign appropriate role based on user type
        Role role = getRoleForUserType(request.getUserType());
        user.setRoles(List.of(role));

        User savedUser = userRepository.save(user);
        log.info("{} user registered successfully: {}", request.getUserType(), savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse changePassword(PasswordChangeRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(currentUser);

        // Optionally blacklist all existing tokens for security
        tokenBlacklistService.blacklistAllUserTokens(currentUser.getEmail(), "PASSWORD_CHANGE");

        log.info("Password changed successfully for user: {}", currentUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse adminResetPassword(AdminPasswordResetRequest request) {
        log.info("Admin password reset for user: {}", request.getUserId());

        // Find user by ID
        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        // Blacklist all existing tokens for security
        tokenBlacklistService.blacklistAllUserTokens(user.getEmail(), "ADMIN_PASSWORD_RESET");

        log.info("Admin password reset successful for user: {} by admin", user.getEmail());

        return userMapper.toResponse(savedUser);
    }

    // Helper method to extract token from Authorization header
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return null;
    }

    private User createUserFromRequest(RegisterRequest request) {
        return switch (request.getUserType()) {
            case CUSTOMER -> registrationMapper.toCustomerEntity(request);
            case BUSINESS_USER -> registrationMapper.toBusinessOwnerEntity(request);
            case PLATFORM_USER -> registrationMapper.toPlatformUserEntity(request);
        };
    }

    private Role getRoleForUserType(UserType userType) {
        RoleEnum roleEnum = switch (userType) {
            case CUSTOMER -> RoleEnum.CUSTOMER;
            case BUSINESS_USER -> RoleEnum.BUSINESS_OWNER;
            case PLATFORM_USER -> RoleEnum.PLATFORM_ADMIN;
        };

        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException(roleEnum.name() + " role not found"));
    }

    @Transactional(readOnly = true)
    private boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    @Transactional(readOnly = true)
    private boolean isPhoneAvailable(String phoneNumber) {
        return !userRepository.existsByPhoneNumberAndIsDeletedFalse(phoneNumber);
    }

    private void validateRegistration(RegisterRequest request) {
        if (!isEmailAvailable(request.getEmail())) {
            throw new ValidationException("Email is already in use");
        }

        if (request.getPhoneNumber() != null && !isPhoneAvailable(request.getPhoneNumber())) {
            throw new ValidationException("Phone number is already in use");
        }
    }
}