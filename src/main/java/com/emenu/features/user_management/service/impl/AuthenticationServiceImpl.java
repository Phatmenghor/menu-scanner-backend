package com.emenu.features.user_management.service.impl;

import com.emenu.enums.*;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.request.LoginRequest;
import com.emenu.features.user_management.dto.request.RefreshTokenRequest;
import com.emenu.features.user_management.dto.request.RegisterRequest;
import com.emenu.features.user_management.dto.response.AuthenticationResponse;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.AuthenticationService;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.utils.validation.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final HttpServletRequest request;

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            // Find user first to check status
            User user = userRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Check if account is locked
            if (user.isLocked()) {
                log.warn("Login attempt for locked account: {}", loginRequest.getEmail());
                throw new LockedException("Account is locked. Please try again later or contact support.");
            }

            // Check if account is active
            if (!user.canLogin()) {
                log.warn("Login attempt for inactive account: {}", loginRequest.getEmail());
                throw new DisabledException("Account is not active. Status: " + user.getAccountStatus().getDescription());
            }

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // Update login information
            LocalDateTime lastLogin = user.getLastLogin();
            user.resetLoginAttempts();
            user.setLastLogin(LocalDateTime.now());
            user.setLastLoginIp(getClientIpAddress());
            user.updateActivity();

            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtGenerator.generateAccessToken(authentication);
            String refreshToken = jwtGenerator.generateRefreshToken(authentication);

            log.info("Login successful for user: {}", user.getEmail());

            return buildAuthenticationResponse(user, accessToken, refreshToken, lastLogin);

        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            handleFailedLogin(loginRequest.getEmail());
            log.warn("Login failed for email: {} - Invalid credentials", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");

        } catch (LockedException | DisabledException e) {
            // Re-throw specific exceptions
            throw e;

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        // Validate request
        validateRegistrationRequest(registerRequest);

        // Create user
        User user = createUserFromRegistration(registerRequest);

        // Assign default role based on user type
        Role defaultRole = getDefaultRoleForUserType(registerRequest.getUserType());
        user.setRoles(List.of(defaultRole));

        User savedUser = userRepository.save(user);

        // Send verification email asynchronously
        sendVerificationEmail(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return AuthenticationResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .fullName(savedUser.getFullName())
                .userType(savedUser.getUserType())
                .roles(savedUser.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()))
                .emailVerified(false)
                .currentLogin(LocalDateTime.now())
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshRequest) {
        log.info("Refresh token request");

        String refreshToken = refreshRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtGenerator.validateToken(refreshToken) || !jwtGenerator.isRefreshToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }

        String username = jwtGenerator.getUsernameFromJWT(refreshToken);
        User user = userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if user is still active
        if (!user.canLogin()) {
            throw new ValidationException("User account is not active");
        }

        // Create new authentication for token generation
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        user.getRoles().stream()
                                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                                .collect(Collectors.toList())
                );

        String newAccessToken = jwtGenerator.generateAccessToken(authentication);
        String newRefreshToken = jwtGenerator.generateRefreshToken(authentication);

        // Update user activity
        user.updateActivity();
        userRepository.save(user);

        return buildAuthenticationResponse(user, newAccessToken, newRefreshToken, user.getLastLogin());
    }

    @Override
    public void logout(String token) {
        log.info("Logout request");

        try {
            String username = jwtGenerator.getUsernameFromJWT(token);
            User user = userRepository.findByEmailAndIsDeletedFalse(username)
                    .orElse(null);

            if (user != null) {
                // Update last active time
                user.updateActivity();
                userRepository.save(user);
                log.info("User logged out successfully: {}", username);
            }
        } catch (Exception e) {
            log.warn("Error during logout: {}", e.getMessage());
        }

        // TODO: Implement token blacklisting
        // For now, just log the logout
        log.info("User logged out successfully");
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Email verification request with token: {}", token);

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        if (user.getEmailVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);

        // Activate account if it was pending verification
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        // Send welcome email
        sendWelcomeEmail(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset request for email: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // Send password reset email
        sendPasswordResetEmail(user, resetToken);

        log.info("Password reset token generated for user: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset with token: {}", token);

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset token has expired");
        }

        ValidationUtils.validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastPasswordChange(LocalDateTime.now());

        // Unlock account if it was locked
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        // Send confirmation email
        sendPasswordChangeConfirmationEmail(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email for: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        // Generate new verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));

        userRepository.save(user);

        // Send verification email
        sendVerificationEmail(user);

        log.info("Verification email resent to: {}", email);
    }

    @Override
    public void requestPhoneVerification(String email) {
        log.info("Phone verification request for: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getPhoneNumber() == null) {
            throw new ValidationException("No phone number associated with this account");
        }

        if (user.isPhoneVerified()) {
            throw new ValidationException("Phone number is already verified");
        }

        // Generate phone verification code
        String verificationCode = generatePhoneVerificationCode();
        user.setPhoneVerificationToken(verificationCode);
        user.setPhoneVerificationExpires(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        // Send SMS verification code (TODO: implement SMS service)
        sendPhoneVerificationSMS(user, verificationCode);

        log.info("Phone verification code sent to user: {}", email);
    }

    @Override
    public void verifyPhone(String email, String code) {
        log.info("Phone verification for: {} with code: {}", email, code);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getPhoneVerificationToken() == null ||
                !user.getPhoneVerificationToken().equals(code)) {
            throw new ValidationException("Invalid verification code");
        }

        if (user.getPhoneVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Verification code has expired");
        }

        user.setPhoneVerified(true);
        user.setPhoneVerificationToken(null);
        user.setPhoneVerificationExpires(null);

        userRepository.save(user);

        log.info("Phone verified successfully for user: {}", email);
    }

    // Private helper methods
    private void validateRegistrationRequest(RegisterRequest request) {
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());

        // Check password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password confirmation does not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Check if phone already exists (if provided)
        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            throw new ValidationException("Phone number already exists");
        }

        // Validate phone number format
        if (request.getPhoneNumber() != null) {
            ValidationUtils.validatePhone(request.getPhoneNumber());
        }

        // Check terms acceptance
        if (!request.isAcceptTerms()) {
            throw new ValidationException("You must accept the terms and conditions");
        }

        if (!request.isAcceptPrivacy()) {
            throw new ValidationException("You must accept the privacy policy");
        }
    }

    private User createUserFromRegistration(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setUserType(request.getUserType());
        user.setCompany(request.getBusinessName());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        user.setMarketingEmails(request.isAcceptMarketing());
        user.setDataProcessingConsent(request.isDataProcessingConsent());
        user.setMarketingConsent(request.isAcceptMarketing());
        user.setRegistrationIp(getClientIpAddress());

        // UTM tracking
        user.setUtmSource(request.getUtmSource());
        user.setUtmMedium(request.getUtmMedium());
        user.setUtmCampaign(request.getUtmCampaign());

        // Handle referral
        if (request.getReferralCode() != null) {
            User referrer = userRepository.findByReferralCode(request.getReferralCode())
                    .orElse(null);
            if (referrer != null) {
                user.setReferredByUserId(referrer.getId());
            }
        }

        // Generate verification tokens
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));

        if (user.getPhoneNumber() != null) {
            user.setPhoneVerificationToken(generatePhoneVerificationCode());
            user.setPhoneVerificationExpires(LocalDateTime.now().plusMinutes(15));
        }

        // Accept terms
        user.acceptTermsAndPrivacy();

        // Set default subscription for business users
        if (user.getUserType() == UserType.BUSINESS_USER) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            user.setSubscriptionStarts(LocalDateTime.now());
            user.setSubscriptionEnds(LocalDateTime.now().plusDays(SubscriptionPlan.FREE.getDefaultDurationDays()));
        }

        return user;
    }

    private Role getDefaultRoleForUserType(UserType userType) {
        RoleEnum roleEnum = switch (userType) {
            case PLATFORM_USER -> RoleEnum.PLATFORM_STAFF; // Default to staff, admin will assign higher roles
            case BUSINESS_USER -> RoleEnum.BUSINESS_OWNER; // Business registration defaults to owner
            case CUSTOMER -> RoleEnum.CUSTOMER;
            case GUEST -> RoleEnum.GUEST_CUSTOMER;
        };

        return roleRepository.findByNameAndIsDeletedFalse(roleEnum)
                .orElseThrow(() -> new ValidationException("Default role not found: " + roleEnum));
    }

    private void handleFailedLogin(String email) {
        userRepository.findByEmailAndIsDeletedFalse(email)
                .ifPresent(user -> {
                    user.incrementLoginAttempts();
                    userRepository.save(user);

                    // Send security alert if account gets locked
                    if (user.isLocked()) {
                        sendAccountLockedEmail(user);
                    }
                });
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String accessToken, String refreshToken, LocalDateTime lastLogin) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000) // 24 hours in milliseconds
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()))
                .emailVerified(user.isEmailVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .businessId(user.getBusinessId())
                .businessName(user.getCompany()) // This would be fetched from business entity in real implementation
                .lastLogin(lastLogin)
                .currentLogin(user.getLastLogin())
                .build();
    }

    private String generatePhoneVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Async notification methods
    @Async
    private void sendVerificationEmail(User user) {
        // TODO: Implement email verification service
        log.info("Sending verification email to: {}", user.getEmail());
    }

    @Async
    private void sendWelcomeEmail(User user) {
        // TODO: Implement welcome email service
        log.info("Sending welcome email to: {}", user.getEmail());
    }

    @Async
    private void sendPasswordResetEmail(User user, String token) {
        // TODO: Implement password reset email service
        log.info("Sending password reset email to: {}", user.getEmail());
    }

    @Async
    private void sendPasswordChangeConfirmationEmail(User user) {
        // TODO: Implement password change confirmation email service
        log.info("Sending password change confirmation email to: {}", user.getEmail());
    }

    @Async
    private void sendAccountLockedEmail(User user) {
        // TODO: Implement account locked email service
        log.info("Sending account locked email to: {}", user.getEmail());
    }

    @Async
    private void sendPhoneVerificationSMS(User user, String code) {
        // TODO: Implement SMS service for phone verification
        log.info("Sending phone verification SMS to: {} with code: {}", user.getPhoneNumber(), code);
    }
}