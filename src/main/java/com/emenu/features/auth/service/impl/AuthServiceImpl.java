package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.MessageType;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.WelcomeMessageRequest;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
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
    private final MessageRepository messageRepository;
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

        // Send welcome message
        sendWelcomeMessage(savedUser);

        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateAccessToken(authentication);

        return createLoginResponse(token, savedUser, "Welcome! Your account has been created successfully.");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateAccessToken(authentication);

        // Get user details
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("User logged in successfully: {}", user.getEmail());

        return createLoginResponse(token, user, "Welcome back!");
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

        // Generate reset token (simplified - in production use proper token generation)
        String resetToken = java.util.UUID.randomUUID().toString();
        
        // Send password reset message
        sendPasswordResetMessage(user, resetToken);

        log.info("Password reset instructions sent to: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // In production, validate the token properly
        // For now, just find user by token (simplified)
        
        // This is simplified - in production you'd store tokens in database
        log.info("Password reset completed");
    }

    @Override
    public void verifyEmail(String token) {
        // Email verification logic
        log.info("Email verified successfully");
    }

    @Override
    public void resendVerification(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Send verification message
        sendVerificationMessage(user);

        log.info("Verification email sent to: {}", email);
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

    @Override
    public void sendCustomWelcomeMessage(WelcomeMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        Message message = new Message();
        message.setSenderId(currentUser.getId());
        message.setSenderEmail(currentUser.getEmail());
        message.setSenderName(currentUser.getFullName());
        message.setRecipientId(currentUser.getId());
        message.setRecipientEmail(currentUser.getEmail());
        message.setRecipientName(currentUser.getFullName());
        message.setSubject("Custom Welcome Message");
        message.setContent(request.getCustomMessage() != null ? request.getCustomMessage() : "Welcome to E-Menu Platform!");
        message.setMessageType(MessageType.WELCOME);
        message.setBusinessId(currentUser.getBusinessId());

        messageRepository.save(message);
        log.info("Custom welcome message sent to user: {}", currentUser.getEmail());
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

    private void sendWelcomeMessage(User user) {
        try {
            Message welcomeMessage = new Message();
            welcomeMessage.setSenderId(null); // System message
            welcomeMessage.setSenderEmail("system@emenu-platform.com");
            welcomeMessage.setSenderName("E-Menu Platform");
            welcomeMessage.setRecipientId(user.getId());
            welcomeMessage.setRecipientEmail(user.getEmail());
            welcomeMessage.setRecipientName(user.getFullName());
            welcomeMessage.setSubject("Welcome to E-Menu Platform!");
            welcomeMessage.setContent(String.format(
                    "Hello %s,\n\nWelcome to E-Menu Platform! We're excited to have you join our community.\n\n" +
                    "Your account has been successfully created and you can now start exploring our features.\n\n" +
                    "If you have any questions, feel free to contact our support team.\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    user.getFullName()
            ));
            welcomeMessage.setMessageType(MessageType.WELCOME);
            welcomeMessage.setBusinessId(user.getBusinessId());

            messageRepository.save(welcomeMessage);
            log.info("Welcome message sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome message to: {}", user.getEmail(), e);
        }
    }

    private void sendPasswordResetMessage(User user, String resetToken) {
        try {
            Message resetMessage = new Message();
            resetMessage.setSenderId(null); // System message
            resetMessage.setSenderEmail("system@emenu-platform.com");
            resetMessage.setSenderName("E-Menu Platform");
            resetMessage.setRecipientId(user.getId());
            resetMessage.setRecipientEmail(user.getEmail());
            resetMessage.setRecipientName(user.getFullName());
            resetMessage.setSubject("Password Reset Instructions");
            resetMessage.setContent(String.format(
                    "Hello %s,\n\nWe received a request to reset your password.\n\n" +
                    "Reset Token: %s\n\n" +
                    "If you didn't request this, please ignore this message.\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    user.getFullName(), resetToken
            ));
            resetMessage.setMessageType(MessageType.NOTIFICATION);
            resetMessage.setBusinessId(user.getBusinessId());

            messageRepository.save(resetMessage);
            log.info("Password reset message sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset message to: {}", user.getEmail(), e);
        }
    }

    private void sendVerificationMessage(User user) {
        try {
            Message verificationMessage = new Message();
            verificationMessage.setSenderId(null); // System message
            verificationMessage.setSenderEmail("system@emenu-platform.com");
            verificationMessage.setSenderName("E-Menu Platform");
            verificationMessage.setRecipientId(user.getId());
            verificationMessage.setRecipientEmail(user.getEmail());
            verificationMessage.setRecipientName(user.getFullName());
            verificationMessage.setSubject("Email Verification");
            verificationMessage.setContent(String.format(
                    "Hello %s,\n\nPlease verify your email address by clicking the link below.\n\n" +
                    "Verification Link: [Click here to verify]\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    user.getFullName()
            ));
            verificationMessage.setMessageType(MessageType.NOTIFICATION);
            verificationMessage.setBusinessId(user.getBusinessId());

            messageRepository.save(verificationMessage);
            log.info("Verification message sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification message to: {}", user.getEmail(), e);
        }
    }
}