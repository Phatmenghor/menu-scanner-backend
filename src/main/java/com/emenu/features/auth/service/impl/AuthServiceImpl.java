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
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.AuthService;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.subdomain.service.SubdomainService;
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
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessService businessService; // ✅ ADDED: Business service
    private final SubdomainService subdomainService; // ✅ ADDED: Subdomain service
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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found"));

        securityUtils.validateAccountStatus(user);
        String token = jwtGenerator.generateAccessToken(authentication);
        LoginResponse response = authMapper.toLoginResponse(user, token);

        log.info("Login successful for user: {}", user.getEmail());
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

        String userEmail;
        try {
            userEmail = jwtGenerator.getUsernameFromJWT(token);
            log.info("Processing logout for user: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to extract username from token during logout: {}", e.getMessage());
            throw new ValidationException("Invalid token - cannot extract user information");
        }

        tokenBlacklistService.blacklistToken(token, userEmail, "LOGOUT");
        log.info("Logout successful for user: {} - token blacklisted", userEmail);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new {} user: {}", request.getUserType(), request.getEmail());

        validateRegistration(request);

        // ✅ ENHANCED: Handle business user registration with subdomain
        if (request.getUserType() == UserType.BUSINESS_USER) {
            return registerBusinessUser(request);
        } else {
            return registerRegularUser(request);
        }
    }

    // ✅ ADDED: Method to handle business user registration
    private UserResponse registerBusinessUser(RegisterRequest request) {
        log.info("Registering business user with business creation: {}", request.getEmail());

        // Create business first
        BusinessCreateRequest businessRequest = new BusinessCreateRequest();
        businessRequest.setName(request.getBusinessName() != null ? request.getBusinessName() : 
                               request.getFirstName() + "'s Business");
        businessRequest.setEmail(request.getBusinessEmail());
        businessRequest.setPhone(request.getBusinessPhone());
        businessRequest.setAddress(request.getBusinessAddress());
        businessRequest.setDescription(request.getBusinessDescription());

        var businessResponse = businessService.createBusiness(businessRequest);
        log.info("Business created for user registration: {}", businessResponse.getName());

        // Create user with business association
        User user = createUserFromRequest(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBusinessId(businessResponse.getId());

        Role role = getRoleForUserType(request.getUserType());
        user.setRoles(List.of(role));

        User savedUser = userRepository.save(user);

        // ✅ ADDED: Create subdomain if preferred subdomain is provided
        if (StringUtils.hasText(request.getPreferredSubdomain())) {
            try {
                subdomainService.createSubdomainForBusiness(businessResponse.getId(), request.getPreferredSubdomain());
                log.info("Subdomain created for business user: {} with subdomain: {}", 
                        savedUser.getEmail(), request.getPreferredSubdomain());
            } catch (Exception e) {
                log.warn("Failed to create preferred subdomain for business user: {} - Error: {}", 
                        savedUser.getEmail(), e.getMessage());
            }
        }

        log.info("Business user registered successfully: {} with business: {}", 
                savedUser.getEmail(), businessResponse.getName());
        return userMapper.toResponse(savedUser);
    }

    // ✅ ADDED: Method to handle regular user registration
    private UserResponse registerRegularUser(RegisterRequest request) {
        User user = createUserFromRequest(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = getRoleForUserType(request.getUserType());
        user.setRoles(List.of(role));

        User savedUser = userRepository.save(user);
        log.info("{} user registered successfully: {}", request.getUserType(), savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

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

        tokenBlacklistService.blacklistAllUserTokens(currentUser.getEmail(), "PASSWORD_CHANGE");
        log.info("Password changed successfully for user: {}", currentUser.getEmail());

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

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        tokenBlacklistService.blacklistAllUserTokens(user.getEmail(), "ADMIN_PASSWORD_RESET");
        log.info("Admin password reset successful for user: {} by admin", user.getEmail());

        return userMapper.toResponse(savedUser);
    }

    // Helper methods remain the same...
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

        // ✅ ADDED: Validate business-specific fields for business users
        if (request.getUserType() == UserType.BUSINESS_USER) {
            if (!StringUtils.hasText(request.getBusinessName())) {
                throw new ValidationException("Business name is required for business user registration");
            }
        }
    }
}