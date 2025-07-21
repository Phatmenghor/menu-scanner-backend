package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Get user details
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Additional security validation
        securityUtils.validateAccountStatus(user);

        // Generate JWT token
        String token = jwtGenerator.generateAccessToken(authentication);

        // Create response
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType());
        response.setRoles(user.getRoles().stream().map(role -> role.getName().name()).toList());
        response.setBusinessId(user.getBusinessId());
        response.setBusinessName(user.getBusiness() != null ? user.getBusiness().getName() : null);
        response.setWelcomeMessage("Welcome back, " + user.getFirstName() + "!");

        log.info("Login successful for user: {}", user.getEmail());
        return response;
    }

    @Override
    public void logout(String token) {
        // In a more complete implementation, you would add the token to a blacklist
        log.info("User logged out");
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Implementation for refresh token logic
        throw new RuntimeException("Refresh token not implemented yet");
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        validateRegistration(request);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setUserType(UserType.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Assign customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);
        log.info("Customer registered successfully: {}", savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse registerBusinessOwner(RegisterRequest request) {
        log.info("Registering new business owner: {}", request.getEmail());

        validateRegistration(request);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setUserType(UserType.BUSINESS_USER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Assign business owner role
        Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                .orElseThrow(() -> new RuntimeException("Business owner role not found"));
        user.setRoles(List.of(businessOwnerRole));

        User savedUser = userRepository.save(user);
        log.info("Business owner registered successfully: {}", savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse registerCustomer(RegisterRequest request) {
        return register(request);
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password confirmation does not match");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User user = userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate reset token and send email
        // Implementation would involve generating a secure token and sending email
        log.info("Password reset requested for user: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implementation for password reset with token validation
        log.info("Password reset attempted with token");
    }

    @Override
    public void sendEmailVerification(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate verification token and send email
        log.info("Email verification sent to user: {}", user.getEmail());
    }

    @Override
    public void verifyEmail(String token) {
        // Implementation for email verification
        log.info("Email verification attempted with token");
    }

    @Override
    public void lockAccount(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        log.info("Account locked for user: {}", user.getEmail());
    }

    @Override
    public void unlockAccount(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account unlocked for user: {}", user.getEmail());
    }

    @Override
    public void suspendAccount(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        log.info("Account suspended for user: {}", user.getEmail());
    }

    @Override
    public void activateAccount(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account activated for user: {}", user.getEmail());
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
            throw new RuntimeException("Email is already in use");
        }

        if (request.getPhoneNumber() != null && !isPhoneAvailable(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number is already in use");
        }
    }
}