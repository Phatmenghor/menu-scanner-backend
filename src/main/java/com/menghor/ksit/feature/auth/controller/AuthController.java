package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.constants.SuccessMessages;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.dto.request.LoginRequestDto;
import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.auth.mapper.StudentMapper;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.service.AuthService;
import com.menghor.ksit.feature.auth.service.LogoutService;
import com.menghor.ksit.feature.auth.service.StaffService;
import com.menghor.ksit.feature.auth.service.StudentService;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;
    private final SecurityUtils securityUtils;
    private final StudentService studentService;
    private final StaffService staffService;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Attempting login for username: {}", loginRequestDto.getUsername());
        AuthResponseDto authResponse = authService.login(loginRequestDto);
        log.info("Login successful for username: {}", loginRequestDto.getUsername());
        return new ApiResponse<>("success", "Login successful", authResponse);
    }

    @PostMapping("/change-password-by-admin")
    public ApiResponse<StudentUserResponseDto> changePasswordStudentByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.info("Admin changing password for user student ID: {}", changePasswordDto.getId());
        StudentUserResponseDto user = authService.changePasswordStudentByAdmin(changePasswordDto);
        return new ApiResponse<>("success", SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/change-password")
    public ApiResponse<StaffUserResponseDto> changePasswordStaff(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Changing password for user staff");
        StaffUserResponseDto user = authService.changePasswordStaff(changePasswordDto);
        return new ApiResponse<>("success", SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/student/token")
    public ApiResponse<StudentUserResponseDto> getStudentByToken() {
        log.info("Getting detailed student user information by token");

        final UserEntity currentEntity = securityUtils.getCurrentUser();

        // Use the service to get detailed information instead of just mapping
        StudentUserResponseDto user = studentService.getStudentUserById(currentEntity.getId());

        log.info("Detailed student user information retrieved successfully for user ID: {}", currentEntity.getId());
        return new ApiResponse<>("success", "Detailed student user information retrieved successfully", user);
    }

    @PostMapping("/staff/token")
    public ApiResponse<StaffUserResponseDto> getStaffByToken() {
        log.info("Getting detailed staff user information by token");

        final UserEntity currentEntity = securityUtils.getCurrentUser();

        // Use the service to get detailed information instead of just mapping
        StaffUserResponseDto user = staffService.getStaffUserById(currentEntity.getId());

        log.info("Detailed staff user information retrieved successfully for user ID: {}", currentEntity.getId());
        return new ApiResponse<>("success", "Detailed staff user information retrieved successfully", user);
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponseDto> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            log.info("Refreshing token...");
            AuthResponseDto newToken = authService.refreshToken(refreshToken);
            log.info("Token refreshed successfully");
            return new ApiResponse<>("success", "Token refreshed successfully", newToken);
        } else {
            log.warn("Invalid or missing Authorization header during token refresh");
            return new ApiResponse<>("error", "Authorization header missing or invalid", null);
        }
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Logging out token...");
            logoutService.logout(token);
            log.info("Logout successful");
            return new ApiResponse<>("success", "Logged out successfully", null);
        } else {
            log.warn("Invalid or missing Authorization header during logout");
            return new ApiResponse<>("error", "Authorization header missing or invalid", null);
        }
    }
}