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
        log.info("Attempting login for email: {}", loginRequestDto.getUsername());

        // Validate input
        if (!StringUtils.hasText(loginRequestDto.getUsername()) || !StringUtils.hasText(loginRequestDto.getPassword())) {
            log.warn("Login attempt with missing credentials");
            throw new BadRequestException("Email and password are required");
        }

        try {
            // Check if user exists and is active before authentication
            UserEntity user = userRepository.findByUsername(loginRequestDto.getUsername())
                    .orElseThrow(() -> {
                        log.warn("Login attempt with non-existent email: {}", loginRequestDto.getUsername());
                        return new BadCredentialsException("Invalid email or password");
                    });

            // Check user status
            validateUserStatus(user);

            // Attempt authentication
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

            log.info("Login successful for email: {} with roles: {}", loginRequestDto.getUsername(), roles);

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
            log.warn("Authentication failed for email: {} - Invalid credentials", loginRequestDto.getUsername());
            throw new BadCredentialsException("Invalid email or password. Please check your credentials and try again.");
        } catch (DisabledException ex) {
            log.warn("Authentication failed for email: {} - Account disabled", loginRequestDto.getUsername());
            throw new DisabledException("Your account has been disabled. Please contact administrator for assistance.");
        } catch (LockedException ex) {
            log.warn("Authentication failed for email: {} - Account locked", loginRequestDto.getUsername());
            throw new LockedException("Your account has been locked. Please contact administrator for assistance.");
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email: {} - {}", loginRequestDto.getUsername(), ex.getMessage());
            throw new BadCredentialsException("Authentication failed. Please check your credentials and try again.");
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
                throw new BadRequestException("Invalid refresh token");
            }

            String username = jwtGenerator.getUsernameFromJWT(refreshToken);
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Token refresh failed: User not found for username: {}", username);
                        return new NotFoundException("User not found");
                    });

            // Check user status
            validateUserStatus(userEntity);

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
            throw new BadRequestException("Refresh token has expired. Please login again.");
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new BadRequestException("Token refresh failed. Please login again.");
        }
    }

    @Override
    public StaffUserResponseDto changePasswordStaff(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for staff user");

        validatePasswordChangeRequest(requestDto);

        UserEntity user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password is incorrect for user ID: {}", user.getId());
            throw new BadRequestException(ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", user.getId());
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", user.getId());

        return staffMapper.toStaffUserDto(updatedUser);
    }

    @Override
    public StudentUserResponseDto changePasswordStudentByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user ID: {}", requestDto.getId());

        validateAdminPasswordChangeRequest(requestDto);

        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", requestDto.getId());
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, requestDto.getId()));
                });

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", requestDto.getId());
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", requestDto.getId());

        return studentMapper.toStudentUserDto(updatedUser);
    }

    // Helper methods for validation
    private void validateUserStatus(UserEntity user) {
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        switch (user.getStatus()) {
            case DELETED:
                log.warn("Login attempt for deleted user: {}", user.getUsername());
                throw new LockedException("Your account has been deleted. Please contact administrator.");
            case INACTIVE:
                log.warn("Login attempt for inactive user: {}", user.getUsername());
                throw new DisabledException("Your account is inactive. Please contact administrator to activate your account.");
            case ACTIVE:
                // User is active, continue
                break;
            default:
                log.warn("Login attempt for user with unknown status: {} - {}", user.getUsername(), user.getStatus());
                throw new DisabledException("Your account status is invalid. Please contact administrator.");
        }
    }

    private void validatePasswordChangeRequest(ChangePasswordRequestDto requestDto) {
        if (!StringUtils.hasText(requestDto.getCurrentPassword())) {
            throw new BadRequestException("Current password is required");
        }
        if (!StringUtils.hasText(requestDto.getNewPassword())) {
            throw new BadRequestException("New password is required");
        }
        if (!StringUtils.hasText(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException("Confirm password is required");
        }
        if (requestDto.getNewPassword().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long");
        }
    }

    private void validateAdminPasswordChangeRequest(ChangePasswordByAdminRequestDto requestDto) {
        if (requestDto.getId() == null) {
            throw new BadRequestException("User ID is required");
        }
        if (!StringUtils.hasText(requestDto.getNewPassword())) {
            throw new BadRequestException("New password is required");
        }
        if (!StringUtils.hasText(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException("Confirm password is required");
        }
        if (requestDto.getNewPassword().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long");
        }
    }
}
