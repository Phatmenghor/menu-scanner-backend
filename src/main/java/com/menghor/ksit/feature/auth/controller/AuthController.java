package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.UserRegisterDto;
import com.menghor.ksit.feature.auth.dto.request.StudentRegisterDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.LoginResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
import com.menghor.ksit.feature.auth.service.AuthService;
import com.menghor.ksit.feature.auth.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDto> login(@Valid @RequestBody LoginResponseDto loginResponseDto) {
        log.info("Attempting login for username: {}", loginResponseDto.getEmail());
        AuthResponseDto authResponse = authService.login(loginResponseDto);
        log.info("Login successful for username: {}", loginResponseDto.getEmail());
        return new ApiResponse<>("success", "Login successful", authResponse);
    }

    @PostMapping("/register")
    public ApiResponse<UserDetailsDto> registerUsers(@Valid @RequestBody UserRegisterDto registerDto) {
        log.info("Registering advanced user with email: {}", registerDto.getEmail());
        UserDetailsDto registeredUser = authService.registerAdvanced(registerDto);
        log.info("User registered successfully with ID: {}", registeredUser.getId());
        return new ApiResponse<>("success", "Users registered successfully", registeredUser);
    }

    @PostMapping("/register/student")
    public ApiResponse<UserDetailsDto> registerStudent(@Valid @RequestBody StudentRegisterDto registerDto) {
        log.info("Registering student with email: {}", registerDto.getEmail());
        UserDetailsDto registeredStudent = authService.registerStudent(registerDto);
        log.info("Student registered successfully with ID: {}", registeredStudent.getId());
        return new ApiResponse<>("success", "Student registered successfully", registeredStudent);
    }

    @PostMapping("/create/users")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsDto> createdUser(@Valid @RequestBody UserRegisterDto registerDto) {
        registerDto.setRole(RoleEnum.STAFF);
        log.info("Creating staff user with email: {}", registerDto.getEmail());
        UserDetailsDto registeredStaff = authService.registerAdvanced(registerDto);
        log.info("Staff user created successfully with ID: {}", registeredStaff.getId());
        return new ApiResponse<>("success", "Staff registered successfully", registeredStaff);
    }

    @PostMapping("/create/students")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<UserDetailsDto> createdStudent(@Valid @RequestBody StudentRegisterDto registerDto) {
        log.info("Creating student by staff/admin with email: {}", registerDto.getEmail());
        UserDetailsDto registeredStudent = authService.registerStudent(registerDto);
        log.info("Student created successfully with ID: {}", registeredStudent.getId());
        return new ApiResponse<>("success", "Staff registered successfully", registeredStudent);
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
