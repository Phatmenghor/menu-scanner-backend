package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.dto.request.LoginRequestDto;
import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.auth.mapper.StudentMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.auth.service.AuthService;
import com.menghor.ksit.utils.database.SecurityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;
    private final StudentMapper studentMapper;
    private final StaffMapper staffMapper;

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for username: {}", loginRequestDto.getUsername());

        // Validate input
        if (!StringUtils.hasText(loginRequestDto.getUsername()) || !StringUtils.hasText(loginRequestDto.getPassword())) {
            log.warn("Login attempt with missing credentials");
            throw new BadRequestException("Username and password are required");
        }

        try {
            // First, check if user exists before authentication
            UserEntity user = userRepository.findByUsername(loginRequestDto.getUsername())
                    .orElse(null);

            if (user == null) {
                log.warn("Login attempt with non-existent username: {}", loginRequestDto.getUsername());
                throw new BadCredentialsException("Invalid username or password. Please check your credentials and try again.");
            }

            // Check user status before authentication
            validateUserStatusForLogin(user, loginRequestDto.getPassword());

            // Attempt authentication only if user exists and status is valid
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            // Extract roles
            List<RoleEnum> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            log.info("Login successful for username: {} with roles: {}", loginRequestDto.getUsername(), roles);

            // Use builder to create response with user information
            return AuthResponseDto.builder()
                    .accessToken(token)
                    .tokenType("Bearer ")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException ex) {
            // This exception can be thrown by Spring Security if password doesn't match
            log.warn("Authentication failed for username: {} - Invalid credentials", loginRequestDto.getUsername());

            // Check if user exists to provide more specific error
            UserEntity user = userRepository.findByUsername(loginRequestDto.getUsername()).orElse(null);
            if (user != null) {
                // User exists but password is wrong
                throw new BadCredentialsException("Incorrect password. Please check your password and try again.");
            } else {
                // User doesn't exist
                throw new BadCredentialsException("Invalid username or password. Please check your credentials and try again.");
            }
        } catch (DisabledException ex) {
            log.warn("Authentication failed for username: {} - Account disabled", loginRequestDto.getUsername());
            throw ex; // Re-throw with original message
        } catch (LockedException ex) {
            log.warn("Authentication failed for username: {} - Account locked", loginRequestDto.getUsername());
            throw ex; // Re-throw with original message
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for username: {} - {}", loginRequestDto.getUsername(), ex.getMessage());
            throw new BadCredentialsException("Authentication failed. Please check your credentials and try again.");
        }
    }

    /**
     * Enhanced user status validation with specific error messages
     */
    private void validateUserStatusForLogin(UserEntity user, String password) {
        // Set default status if null
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
            return;
        }

        // Check status and provide specific error messages
        switch (user.getStatus()) {
            case DELETED:
                log.warn("Login attempt for deleted user: {}", user.getUsername());
                throw new LockedException("Your account has been permanently deleted and cannot be recovered. Please contact the administrator if you believe this is an error.");

            case INACTIVE:
                log.warn("Login attempt for inactive user: {}", user.getUsername());
                throw new DisabledException("Your account has been temporarily deactivated. Please contact the administrator to reactivate your account.");

            case ACTIVE:
                // User is active, check if password might be wrong
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    log.warn("Password mismatch for active user: {}", user.getUsername());
                    // Don't throw here, let Spring Security handle it for security reasons
                    // This is just for logging purposes
                }
                break;

            default:
                log.warn("Login attempt for user with unknown status: {} - {}", user.getUsername(), user.getStatus());
                throw new DisabledException("Your account status is invalid. Please contact the administrator for assistance.");
        }
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        log.info("Attempting token refresh");

        if (!StringUtils.hasText(refreshToken)) {
            throw new BadRequestException("Refresh token is required");
        }

        try {
            // Validate the refresh token
            if (!jwtGenerator.validateToken(refreshToken)) {
                throw new BadRequestException("Invalid or expired refresh token. Please login again.");
            }

            String username = jwtGenerator.getUsernameFromJWT(refreshToken);
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Token refresh failed: User not found for username: {}", username);
                        return new NotFoundException("User account no longer exists. Please login again.");
                    });

            // Check user status for token refresh
            validateUserStatusForTokenRefresh(userEntity);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            String newToken = jwtGenerator.generateToken(authentication);

            // Extract roles
            List<RoleEnum> roles = userEntity.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            log.info("Token refreshed successfully for user: {}", username);

            return AuthResponseDto.builder()
                    .accessToken(newToken)
                    .tokenType("Bearer ")
                    .userId(userEntity.getId())
                    .username(userEntity.getUsername())
                    .email(userEntity.getEmail())
                    .roles(roles)
                    .build();

        } catch (ExpiredJwtException ex) {
            log.warn("Token refresh failed: Refresh token expired");
            throw new BadRequestException("Your session has completely expired. Please login again to continue.");
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new BadRequestException("Unable to refresh token. Please login again.");
        }
    }

    /**
     * Validate user status for token refresh
     */
    private void validateUserStatusForTokenRefresh(UserEntity user) {
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
            return;
        }

        switch (user.getStatus()) {
            case DELETED:
                throw new BadRequestException("Your account has been deleted. Please contact the administrator.");
            case INACTIVE:
                throw new BadRequestException("Your account has been deactivated. Please contact the administrator to reactivate your account.");
            case ACTIVE:
                // User is active, continue
                break;
            default:
                throw new BadRequestException("Your account status is invalid. Please contact the administrator.");
        }
    }

    @Override
    public StaffUserResponseDto changePasswordStaff(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for staff user");

        validatePasswordChangeRequest(requestDto);

        UserEntity user = securityUtils.getCurrentUser();

        // Validate current password
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password is incorrect for user ID: {}", user.getId());
            throw new BadRequestException("The current password you entered is incorrect. Please enter your current password correctly.");
        }

        // Validate new password confirmation
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", user.getId());
            throw new BadRequestException("The new password and confirmation password do not match. Please ensure both passwords are identical.");
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            log.warn("User tried to set same password as current for user ID: {}", user.getId());
            throw new BadRequestException("The new password cannot be the same as your current password. Please choose a different password.");
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", user.getId());

        return staffMapper.toStaffUserDto(updatedUser);
    }

    @Override
    public StudentUserResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user ID: {}", requestDto.getId());

        validateAdminPasswordChangeRequest(requestDto);

        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", requestDto.getId());
                    return new NotFoundException("Student not found with ID: " + requestDto.getId() + ". The student may have been deleted or does not exist.");
                });

        // Validate new password confirmation
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", requestDto.getId());
            throw new BadRequestException("The new password and confirmation password do not match. Please ensure both passwords are identical.");
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            log.warn("Admin tried to set same password as current for user ID: {}", requestDto.getId());
            throw new BadRequestException("The new password cannot be the same as the current password. Please choose a different password.");
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully by admin for user ID: {}", requestDto.getId());

        return studentMapper.toStudentUserDto(updatedUser);
    }

    // Enhanced validation methods with better error messages
    private void validatePasswordChangeRequest(ChangePasswordRequestDto requestDto) {
        if (!StringUtils.hasText(requestDto.getCurrentPassword())) {
            throw new BadRequestException("Current password is required and cannot be empty.");
        }
        if (!StringUtils.hasText(requestDto.getNewPassword())) {
            throw new BadRequestException("New password is required and cannot be empty.");
        }
        if (!StringUtils.hasText(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException("Password confirmation is required and cannot be empty.");
        }
        if (requestDto.getNewPassword().length() < 3) {
            throw new BadRequestException("New password must be at least 3 characters long for security.");
        }
        if (requestDto.getNewPassword().length() > 128) {
            throw new BadRequestException("New password cannot exceed 128 characters.");
        }
        // Check if new password contains only whitespace
        if (requestDto.getNewPassword().trim().isEmpty()) {
            throw new BadRequestException("New password cannot contain only whitespace characters.");
        }
    }

    private void validateAdminPasswordChangeRequest(ChangePasswordByAdminRequestDto requestDto) {
        if (requestDto.getId() == null) {
            throw new BadRequestException("User ID is required to change password.");
        }
        if (requestDto.getId() <= 0) {
            throw new BadRequestException("Invalid user ID. User ID must be a positive number.");
        }
        if (!StringUtils.hasText(requestDto.getNewPassword())) {
            throw new BadRequestException("New password is required and cannot be empty.");
        }
        if (!StringUtils.hasText(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException("Password confirmation is required and cannot be empty.");
        }
        if (requestDto.getNewPassword().length() < 3) {
            throw new BadRequestException("New password must be at least 3 characters long for security.");
        }
        if (requestDto.getNewPassword().length() > 128) {
            throw new BadRequestException("New password cannot exceed 128 characters.");
        }
        // Check if new password contains only whitespace
        if (requestDto.getNewPassword().trim().isEmpty()) {
            throw new BadRequestException("New password cannot contain only whitespace characters.");
        }
    }
}