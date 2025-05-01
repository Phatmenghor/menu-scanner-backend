package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.feature.auth.dto.request.*;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.LoginResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
import com.menghor.ksit.feature.auth.mapper.UserMapper;
import com.menghor.ksit.feature.auth.mapper.UserRegistrationMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final UserMapper userMapper;
    private final UserRegistrationMapper registrationMapper;

    @Override
    public AuthResponseDto login(LoginResponseDto loginResponseDto) {
        log.info("Attempting login for email: {}", loginResponseDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginResponseDto.getEmail(),
                        loginResponseDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        UserEntity userEntity = userRepository.findByUsername(loginResponseDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found for email: {}", loginResponseDto.getEmail());
                    return new BadRequestException("User not found");
                });

        log.info("Login successful for email: {}", loginResponseDto.getEmail());
        return new AuthResponseDto(token, userMapper.toDto(userEntity));
    }

    @Override
    @Transactional
    public UserDetailsDto registerStudent(StudentRegisterDto registerDto) {
        log.info("Registering new student with email: {}", registerDto.getEmail());
        validateUserRegistration(registerDto.getEmail());

        UserEntity user = registrationMapper.toEntityFromStudentRegister(registerDto);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        user.setRoles(determineRoles(RoleEnum.STUDENT));

        UserEntity savedUser = userRepository.save(user);
        log.info("Student registered successfully with email: {}", registerDto.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDetailsDto registerAdvanced(UserRegisterDto registerDto) {
        log.info("Registering new user (advanced) with email: {}", registerDto.getEmail());
        validateUserRegistration(registerDto.getEmail());

        UserEntity user = registrationMapper.toEntityFromAdvancedRegister(registerDto);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        List<Role> roles = new ArrayList<>();

        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            log.info("Assigning multiple roles to user: {}", registerDto.getRoles());
            for (RoleEnum roleEnum : registerDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> {
                            log.warn("Invalid role during registration: {}", roleEnum);
                            return new BadRequestException("Invalid role: " + roleEnum);
                        });
                roles.add(role);
            }
        } else if (registerDto.getRole() != null) {
            Role role = roleRepository.findByName(registerDto.getRole())
                    .orElseThrow(() -> {
                        log.warn("Invalid single role during registration: {}", registerDto.getRole());
                        return new BadRequestException("Invalid role");
                    });
            roles.add(role);
        } else {
            Role defaultRole = roleRepository.findByName(RoleEnum.STUDENT)
                    .orElseThrow(() -> {
                        log.warn("Default role not found for registration");
                        return new BadRequestException("Default role not found");
                    });
            roles.add(defaultRole);
        }

        user.setRoles(roles);
        UserEntity savedUser = userRepository.save(user);
        log.info("Advanced user registered successfully with email: {}", registerDto.getEmail());
        return userMapper.toDto(savedUser);
    }

    private void validateUserRegistration(String email) {
        if (userRepository.existsByUsername(email)) {
            log.warn("Attempt to register with duplicate email: {}", email);
            throw new DuplicateNameException("Email is already in use");
        }
    }

    private List<Role> determineRoles(RoleEnum roleEnum) {
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleEnum);
                    return new BadRequestException("Invalid role: " + roleEnum);
                });
        return Collections.singletonList(role);
    }

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        log.info("Refreshing token");
        try {
            String username = jwtGenerator.getUsernameFromJWT(refreshToken.substring(7));
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Token refresh failed: User not found for username: {}", username);
                        return new BadRequestException("User not found");
                    });

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            String newToken = jwtGenerator.generateToken(authentication);

            log.info("Token refreshed successfully for user: {}", username);
            return new AuthResponseDto(newToken, userMapper.toDto(userEntity));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BadRequestException("Token refresh failed: " + e.getMessage());
        }
    }
}
