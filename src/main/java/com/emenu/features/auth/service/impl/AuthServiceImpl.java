// src/main/java/com/emenu/features/auth/service/impl/AuthServiceImpl.java
package com.emenu.features.auth.service.impl;

import com.emenu.enums.user.RoleEnum;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.*;
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
import com.emenu.features.notification.service.TelegramNotificationService;
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
    private final TelegramNotificationService telegramNotificationService;

    // ===== TRADITIONAL LOGIN =====
    
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("🔐 Login attempt for userIdentifier: {}", request.getUserIdentifier());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserIdentifier(), request.getPassword())
            );

            User user = userRepository.findByUserIdentifierAndIsDeletedFalse(request.getUserIdentifier())
                    .orElseThrow(() -> new ValidationException("User not found"));

            securityUtils.validateAccountStatus(user);
            
            if (user.hasTelegramLinked()) {
                user.updateTelegramActivity();
                userRepository.save(user);
            }
            
            String token = jwtGenerator.generateAccessToken(authentication);
            LoginResponse response = authMapper.toLoginResponse(user, token);

            log.info("✅ Traditional login successful for user: {}", user.getUserIdentifier());
            return response;
            
        } catch (Exception e) {
            log.warn("❌ Traditional login failed for userIdentifier: {} - {}", request.getUserIdentifier(), e.getMessage());
            throw new ValidationException("Invalid credentials. Please check your user identifier and password.");
        }
    }

    // ===== CUSTOMER REGISTRATION =====
    
    @Override
    public UserResponse registerCustomer(RegisterRequest request) {
        log.info("📝 Customer registration for userIdentifier: {}", request.getUserIdentifier());

        validateCustomerRegistration(request);

        User user = registrationMapper.toCustomerEntity(request);
        user.setUserIdentifier(request.getUserIdentifier());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);
        
        // 📱 Send Telegram notification for customer registration
        telegramNotificationService.sendCustomerRegistrationNotification(savedUser);
        
        log.info("✅ Customer registered successfully: {}", savedUser.getUserIdentifier());
        return userMapper.toResponse(savedUser);
    }

    // ===== LOGOUT =====
    
    @Override
    public void logout(String authorizationHeader) {
        log.info("🚪 Processing logout request");
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
        log.info("✅ Logout successful for user: {} - token blacklisted", userIdentifier);
    }

    // ===== PASSWORD MANAGEMENT =====
    
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

        tokenBlacklistService.blacklistAllUserTokens(currentUser.getUserIdentifier(), "PASSWORD_CHANGE");
        log.info("✅ Password changed successfully for user: {}", currentUser.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse adminResetPassword(AdminPasswordResetRequest request) {
        log.info("🔑 Admin password reset for user: {}", request.getUserId());

        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        tokenBlacklistService.blacklistAllUserTokens(user.getUserIdentifier(), "ADMIN_PASSWORD_RESET");
        log.info("✅ Admin password reset successful for user: {} by admin", user.getUserIdentifier());

        return userMapper.toResponse(savedUser);
    }

    // ===== HELPER METHODS =====
    
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
        if (!isUserIdentifierAvailable(request.getUserIdentifier())) {
            throw new ValidationException("User identifier is already in use");
        }
    }
}