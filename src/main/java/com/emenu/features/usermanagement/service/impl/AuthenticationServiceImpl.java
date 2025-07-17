package com.emenu.features.usermanagement.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.dto.request.LoginRequest;
import com.emenu.features.usermanagement.dto.request.RefreshTokenRequest;
import com.emenu.features.usermanagement.dto.request.RegisterRequest;
import com.emenu.features.usermanagement.dto.response.AuthenticationResponse;
import com.emenu.features.usermanagement.repository.RoleRepository;
import com.emenu.features.usermanagement.repository.UserRepository;
import com.emenu.features.usermanagement.service.AuthenticationService;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.utils.validation.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Get user details
            User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            // Update login information
            LocalDateTime lastLogin = user.getLastLogin();
            user.resetLoginAttempts();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate tokens
            String accessToken = jwtGenerator.generateAccessToken(authentication);
            String refreshToken = jwtGenerator.generateRefreshToken(authentication);
            
            log.info("Login successful for user: {}", user.getEmail());
            
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
                    .twoFactorEnabled(user.getTwoFactorEnabled())
                    .businessId(user.getBusinessId())
                    .lastLogin(lastLogin)
                    .currentLogin(user.getLastLogin())
                    .build();
                    
        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                    .ifPresent(user -> {
                        user.incrementLoginAttempts();
                        userRepository.save(user);
                    });
            
            log.warn("Login failed for email: {} - Invalid credentials", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
            
        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException("Authentication failed");
        }
    }

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        // Validate request
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
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setUserType(request.getUserType());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        user.setMarketingEmails(request.isAcceptMarketing());
        
        // Generate email verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));
        
        // Assign default role based on user type
        Role defaultRole = getDefaultRoleForUserType(request.getUserType());
        user.setRoles(List.of(defaultRole));
        
        User savedUser = userRepository.save(user);
        
        // TODO: Send verification email
        
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
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");
        
        String refreshToken = request.getRefreshToken();
        
        if (!jwtGenerator.validateToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }
        
        String username = jwtGenerator.getUsernameFromJWT(refreshToken);
        User user = userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Create new authentication for token generation
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, List.of());
        
        String newAccessToken = jwtGenerator.generateAccessToken(authentication);
        String newRefreshToken = jwtGenerator.generateRefreshToken(authentication);
        
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()))
                .emailVerified(user.isEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .businessId(user.getBusinessId())
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("Logout request");
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
        user.setAccountStatus(AccountStatus.ACTIVE);
        
        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset request for email: {}", email);
        
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        
        // TODO: Send password reset email
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
        
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    private Role getDefaultRoleForUserType(UserType userType) {
        RoleEnum roleEnum = switch (userType) {
            case PLATFORM_ADMIN -> RoleEnum.PLATFORM_ADMIN;
            case BUSINESS_OWNER -> RoleEnum.BUSINESS_OWNER;
            case BUSINESS_STAFF -> RoleEnum.BUSINESS_STAFF;
            case CUSTOMER -> RoleEnum.CUSTOMER;
            case GUEST -> RoleEnum.GUEST;
        };
        
        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException("Default role not found: " + roleEnum));
    }
}