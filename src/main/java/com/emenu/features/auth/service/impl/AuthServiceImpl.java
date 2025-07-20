package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.exception.custom.*;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.security.SecurityUtils;
import com.emenu.security.jwt.JWTGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;

    @Override
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        // Check if user exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Create customer user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setUserType(UserType.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Set customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);
        log.info("Customer registered successfully: {}", savedUser.getEmail());

        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateAccessToken(authentication);

        return createLoginResponse(token, savedUser, "Welcome! Registration successful.");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // First, get user to check account status before authentication
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid email or password"));

        // Validate account status before attempting authentication
        validateAccountStatusForLogin(user);

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateAccessToken(authentication);

            log.info("User logged in successfully: {} with status: {}", user.getEmail(), user.getAccountStatus());
            return createLoginResponse(token, user, "Welcome back!");

        } catch (Exception e) {
            log.warn("Authentication failed for user: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw e; // Re-throw to let Spring Security handle it
        }
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
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
        userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // In a real application, you would:
        // 1. Generate a password reset token
        // 2. Store it with expiration
        // 3. Send email with reset link

        log.info("Password reset requested for: {}", email);
        // For now, just log it
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        userMapper.updateEntity(request, currentUser);
        User updatedUser = userRepository.save(currentUser);

        log.info("Profile updated successfully for user: {}", currentUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }

    private LoginResponse createLoginResponse(String token, User user, String welcomeMessage) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType());
        response.setBusinessId(user.getBusinessId());
        response.setBusinessName(user.getBusiness() != null ? user.getBusiness().getName() : null);
        response.setWelcomeMessage(welcomeMessage);

        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private void validateAccountStatusForLogin(User user) {
        switch (user.getAccountStatus()) {
            case INACTIVE -> {
                log.warn("Login attempt blocked - inactive account: {}", user.getEmail());
                throw new AccountInactiveException("Your account is inactive. Please contact support to reactivate your account.");
            }
            case LOCKED -> {
                log.warn("Login attempt blocked - locked account: {}", user.getEmail());
                throw new AccountLockedException("Your account has been locked due to security reasons. Please contact support to unlock your account.");
            }
            case SUSPENDED -> {
                log.warn("Login attempt blocked - suspended account: {}", user.getEmail());
                throw new AccountSuspendedException("Your account has been suspended. Please contact support for account reactivation.");
            }
            case ACTIVE -> {
                // Account is active, allow login
                log.debug("Account status validated successfully for user: {}", user.getEmail());
            }
            default -> {
                log.error("Unknown account status for user: {} - Status: {}", user.getEmail(), user.getAccountStatus());
                throw new AccountInactiveException("Account status is invalid. Please contact support.");
            }
        }
    }
}