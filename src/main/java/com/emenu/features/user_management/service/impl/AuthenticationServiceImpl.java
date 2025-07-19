package com.emenu.features.user_management.service.impl;

import com.emenu.enums.*;
import com.emenu.exception.*;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.request.*;
import com.emenu.features.user_management.dto.response.AuthenticationResponse;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.AuthenticationService;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.security.SecurityUtils;
import com.emenu.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if user can login
        if (!user.canLogin()) {
            throw new ValidationException("Account is not active or email not verified");
        }

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Update login info
        user.setLastLogin(LocalDateTime.now());
        user.updateActivity();
        userRepository.save(user);

        // Generate token
        String accessToken = jwtGenerator.generateAccessToken(authentication);

        log.info("Login successful for: {}", user.getEmail());

        return buildAuthResponse(user, accessToken);
    }

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registration attempt for: {}", request.getEmail());

        // Validate
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());

        // Check if email exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUserType(UserType.valueOf(request.getUserType()));
        user.setStatus(Status.PENDING);
        user.setEmailVerified(false);

        // Generate verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));

        // Assign default role
        Role defaultRole = getDefaultRole(user.getUserType());
        user.setRoles(List.of(defaultRole));

        User savedUser = userRepository.save(user);

        // TODO: Send verification email
        log.info("User registered: {}", savedUser.getEmail());

        return AuthenticationResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .userType(savedUser.getUserType().name())
                .emailVerified(false)
                .loginTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Email verification attempt");

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        if (user.getEmailVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Verification token expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);
        user.setStatus(Status.ACTIVE);

        userRepository.save(user);
        log.info("Email verified for: {}", user.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset request for: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // TODO: Send reset email
        log.info("Password reset token generated for: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt");

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset token expired");
        }

        ValidationUtils.validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);

        userRepository.save(user);
        log.info("Password reset successful for: {}", user.getEmail());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        ValidationUtils.validatePassword(request.getNewPassword());

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        log.info("Password changed for: {}", currentUser.getEmail());
    }

    @Override
    public void logout(String token) {
        log.info("Logout attempt");
        // TODO: Implement token blacklisting if needed
    }

    private Role getDefaultRole(UserType userType) {
        RoleEnum roleEnum = switch (userType) {
            case PLATFORM_USER -> RoleEnum.PLATFORM_SUPPORT;
            case BUSINESS_USER -> RoleEnum.BUSINESS_OWNER;
            case CUSTOMER -> RoleEnum.CUSTOMER;
        };

        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException("Default role not found"));
    }

    private AuthenticationResponse buildAuthResponse(User user, String accessToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(86400000) // 24 hours
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType().name())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                .emailVerified(user.getEmailVerified())
                .businessId(user.getBusinessId())
                .loginTime(LocalDateTime.now())
                .build();
    }
}