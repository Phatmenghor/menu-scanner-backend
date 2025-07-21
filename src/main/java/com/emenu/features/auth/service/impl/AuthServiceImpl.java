package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.features.auth.dto.request.AdminPasswordResetRequest;
import com.emenu.features.auth.dto.request.LoginRequest;
import com.emenu.features.auth.dto.request.PasswordChangeRequest;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.AccountStatusUpdateRequest;
import com.emenu.features.auth.mapper.RegistrationMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.notification.mapper.AuthMapper;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    // ✅ All required mappers
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final RegistrationMapper registrationMapper;
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

        // ✅ Use mapper to create response
        LoginResponse response = authMapper.toLoginResponse(user, token);

        log.info("Login successful for user: {}", user.getEmail());
        return response;
    }

    @Override
    public void logout(String token) {
        // In a more complete implementation, you would add the token to a blacklist
        log.info("User logged out");
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new {} user: {}", request.getUserType(), request.getEmail());

        validateRegistration(request);

        // ✅ Use registration mapper based on user type
        User user = createUserFromRequest(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign appropriate role based on user type
        Role role = getRoleForUserType(request.getUserType());
        user.setRoles(List.of(role));

        User savedUser = userRepository.save(user);
        log.info("{} user registered successfully: {}", request.getUserType(), savedUser.getEmail());

        // ✅ Use mapper to create response
        return userMapper.toResponse(savedUser);
    }


    @Override
    public UserResponse changePassword(PasswordChangeRequest request) {
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
        User savedUser = userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getEmail());

        // ✅ Use mapper to create response
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse adminResetPassword(AdminPasswordResetRequest request) {
        log.info("Admin password reset for user: {}", request.getUserId());

        // Find user by ID
        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password confirmation does not match");
        }

        // Update password (no current password validation needed for admin reset)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        log.info("Admin password reset successful for user: {} by admin", user.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateAccountStatus(AccountStatusUpdateRequest request) {
        log.info("Updating account status for user: {} to {}", request.getUserId(), request.getAccountStatus());

        User user = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update account status
        user.setAccountStatus(request.getAccountStatus());
        User savedUser = userRepository.save(user);

        log.info("Account status updated for user: {} to {}",
                user.getEmail(), request.getAccountStatus());

        // ✅ Use mapper to create response
        return userMapper.toResponse(savedUser);
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
                .orElseThrow(() -> new RuntimeException(roleEnum.name() + " role not found"));
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