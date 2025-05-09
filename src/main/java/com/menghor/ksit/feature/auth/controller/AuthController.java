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
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final StudentMapper studentMapper;
    private final StaffMapper staffMapper;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Attempting login for username: {}", loginRequestDto.getEmail());
        AuthResponseDto authResponse = authService.login(loginRequestDto);
        log.info("Login successful for username: {}", loginRequestDto.getEmail());
        return new ApiResponse<>("success", "Login successful", authResponse);
    }

    @PostMapping("/staff/change-password")
    public ApiResponse<StaffUserResponseDto> changePasswordStaff(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Changing password for user staff");
        StaffUserResponseDto user = authService.changePasswordStaff(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/student/change-password-student")
    public ApiResponse<StudentUserResponseDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        log.info("Changing password for current student user");
        StudentUserResponseDto user = authService.changePasswordStudent(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/staff/change-password-by-admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER' , 'STAFF')")
    public ApiResponse<StaffUserResponseDto> changePasswordStaffByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.info("Admin changing password for user ID: {}", changePasswordDto.getId());
        StaffUserResponseDto user = authService.changePasswordStaffByAdmin(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/student/change-password-by-admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER' , 'STAFF')")
    public ApiResponse<StudentUserResponseDto> changePasswordStudentByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        log.info("Admin changing password for user student ID: {}", changePasswordDto.getId());
        StudentUserResponseDto user = authService.changePasswordStudentByAdmin(changePasswordDto);
        return new ApiResponse<>(SuccessMessages.SUCCESS, SuccessMessages.PASSWORD_CHANGED_SUCCESSFULLY, user);
    }

    @PostMapping("/student/token")
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    public ApiResponse<StudentUserResponseDto> getStudentByToken() {
        log.info("get user student by token");
        final UserEntity currentEntity = securityUtils.getCurrentUser();
        StudentUserResponseDto user = studentMapper.toStudentUserDto(currentEntity);
        log.info("get user student by token successfully");
        return new ApiResponse<>(SuccessMessages.SUCCESS, "User student get by token response successfully", user);
    }

    @PostMapping("/staff/token")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<StaffUserResponseDto> getStaffByToken() {
        log.info("get user staff by token");
        final UserEntity currentEntity = securityUtils.getCurrentUser();
        StaffUserResponseDto user = staffMapper.toStaffUserDto(currentEntity);
        log.info("get user staff by token successfully");
        return new ApiResponse<>(SuccessMessages.SUCCESS, "User staff get by token response successfully", user);
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