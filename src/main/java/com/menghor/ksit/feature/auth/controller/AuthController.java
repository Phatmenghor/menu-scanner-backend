package com.menghor.ksit.feature.auth.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.dto.request.RegisterDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.LoginDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.mapper.UserMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.setting.repository.SubscriptionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final UserMapper userMapper;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("login")
    public ApiResponse<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Optional<UserEntity> userEntityOpt = userRepository.findByUsername(loginDto.getEmail());
        if (userEntityOpt.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        UserEntity userEntity = userEntityOpt.get();

        // Additional check for SHOP_ADMIN role
        boolean isShopAdmin = userEntity.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.SHOP_ADMIN);

        if (isShopAdmin) {
            boolean hasActiveSubscription = subscriptionRepository
                    .hasActiveSubscription(userEntity.getId(), LocalDateTime.now());

            if (!hasActiveSubscription) {
                log.warn("Shop admin user {} attempted to login without active subscription",
                        userEntity.getUsername());
                throw new BadRequestException("Your subscription has expired. Please renew to continue.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            UserDto userDto = userMapper.toDto(userEntity);

            return new ApiResponse<>("success",
                    "Login successfully",
                    new AuthResponseDto(token, userDto));

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginDto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Fetch the role safely
        Role role = roleRepository.findByName(registerDto.getRole())
                .orElseThrow(() -> new BadRequestException("Invalid role provided."));

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singletonList(role));
        // Always set a status value
        user.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.INACTIVE);
        // Save the user
        UserEntity savedUser = userRepository.save(user);

        // Return success response
        return new ApiResponse<>("success", "You have registered successfully.", userMapper.toDto(savedUser));
    }
}