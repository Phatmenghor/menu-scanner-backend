package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
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
import java.util.Optional;

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

    @Override
    public AuthResponseDto login(LoginDto loginDto) {
        Optional<UserEntity> userEntityOpt = userRepository.findByUsername(loginDto.getEmail());
        if (userEntityOpt.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        UserEntity userEntity = userEntityOpt.get();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);

            UserDto userDto = userMapper.toDto(userEntity);

            return new AuthResponseDto(token, userDto);

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginDto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public UserDto registerStaff(StaffRegisterDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Create user entity
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        
        // Set common personal information
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setContactNumber(registerDto.getContactNumber());
        
        // Set staff-specific fields
        user.setPosition(registerDto.getPosition());
        user.setDepartment(registerDto.getDepartment());
        user.setEmployeeId(registerDto.getEmployeeId());
        
        // Handle role assignment
        List<Role> roles = new ArrayList<>();
        
        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            // Add multiple roles if provided
            for (RoleEnum roleEnum : registerDto.getRoles()) {
                // Only allow ADMIN, STAFF, DEVELOPER roles
                if (roleEnum != RoleEnum.STUDENT) {
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new BadRequestException("Invalid role provided: " + roleEnum));
                    roles.add(role);
                } else {
                    throw new BadRequestException("Cannot assign STUDENT role in staff registration");
                }
            }
        } else if (registerDto.getRole() != null) {
            // For single role assignment
            if (registerDto.getRole() != RoleEnum.STUDENT) {
                Role role = roleRepository.findByName(registerDto.getRole())
                        .orElseThrow(() -> new BadRequestException("Invalid role provided."));
                roles.add(role);
            } else {
                throw new BadRequestException("Cannot assign STUDENT role in staff registration");
            }
        } else {
            // Default to STAFF role if nothing specified
            Role defaultRole = roleRepository.findByName(RoleEnum.STAFF)
                    .orElseThrow(() -> new BadRequestException("Default role not found."));
            roles.add(defaultRole);
        }
        
        user.setRoles(roles);
        
        // Set status
        user.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.ACTIVE);
        
        // Save the user
        UserEntity savedUser = userRepository.save(user);

        // Return user DTO
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto registerStudent(StudentRegisterDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Create user entity
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        
        // Set common personal information
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setContactNumber(registerDto.getContactNumber());
        
        // Set student-specific fields
        user.setStudentId(registerDto.getStudentId());
        user.setGrade(registerDto.getGrade());
        user.setYearOfAdmission(registerDto.getYearOfAdmission());
        
        // Assign STUDENT role
        Role studentRole = roleRepository.findByName(RoleEnum.STUDENT)
                .orElseThrow(() -> new BadRequestException("STUDENT role not found."));
        user.setRoles(Collections.singletonList(studentRole));
        
        // Set status
        user.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.ACTIVE);
        
        // Save the user
        UserEntity savedUser = userRepository.save(user);

        // Return user DTO
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto register(RegisterDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Create user entity
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        
        // Set personal information
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setContactNumber(registerDto.getContactNumber());
        
        // Set role-specific information based on the role
        if (registerDto.getRole() == RoleEnum.STUDENT || 
                (registerDto.getRoles() != null && registerDto.getRoles().contains(RoleEnum.STUDENT))) {
            user.setStudentId(registerDto.getStudentId());
            user.setGrade(registerDto.getGrade());
            user.setYearOfAdmission(registerDto.getYearOfAdmission());
        } else {
            user.setPosition(registerDto.getPosition());
            user.setDepartment(registerDto.getDepartment());
            user.setEmployeeId(registerDto.getEmployeeId());
        }
        
        // Handle role assignment
        List<Role> roles = new ArrayList<>();
        
        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            // Add multiple roles if provided
            for (RoleEnum roleEnum : registerDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new BadRequestException("Invalid role provided: " + roleEnum));
                roles.add(role);
            }
        } else if (registerDto.getRole() != null) {
            // For single role assignment
            Role role = roleRepository.findByName(registerDto.getRole())
                    .orElseThrow(() -> new BadRequestException("Invalid role provided."));
            roles.add(role);
        } else {
            // Default to STUDENT role if nothing specified
            Role defaultRole = roleRepository.findByName(RoleEnum.STUDENT)
                    .orElseThrow(() -> new BadRequestException("Default role not found."));
            roles.add(defaultRole);
        }
        
        user.setRoles(roles);
        
        // Always set a status value
        user.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.ACTIVE);
        
        // Save the user
        UserEntity savedUser = userRepository.save(user);

        // Return success response
        return userMapper.toDto(savedUser);
    }
}