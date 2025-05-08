package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
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
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.auth.service.AuthService;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
        log.info("Attempting login for email: {}", loginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        log.info("Login successful for email: {}", loginRequestDto.getEmail());

        return new AuthResponseDto(token);
    }


    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        log.info("Refreshing token");
        try {
            String username = jwtGenerator.getUsernameFromJWT(refreshToken);
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Token refresh failed: User not found for username: {}", username);
                        return new BadRequestException("User not found");
                    });

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            String newToken = jwtGenerator.generateToken(authentication);

            log.info("Token refreshed successfully for user: {}", username);
            return new AuthResponseDto(newToken);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BadRequestException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public StaffUserResponseDto changePasswordStaff(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for staff user");

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
    public StudentUserResponseDto changePasswordStudent(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for student user");

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

        return studentMapper.toStudentUserDto(updatedUser);
    }

    @Override
    public StaffUserResponseDto changePasswordStaffByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user ID: {}", requestDto.getId());

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

        return staffMapper.toStaffUserDto(updatedUser);
    }

    @Override
    public StudentUserResponseDto changePasswordStudentByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Admin changing password for user ID: {}", requestDto.getId());

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
}