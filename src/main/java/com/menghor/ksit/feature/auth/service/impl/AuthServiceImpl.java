package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentRegisterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffRegisterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.AuthResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsResponseDto;
import com.menghor.ksit.feature.auth.dto.request.LoginRequestDto;
import com.menghor.ksit.feature.auth.mapper.UserMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.auth.service.AuthService;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.utils.database.SecurityUtils;
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
    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for email: {}", loginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        UserEntity userEntity = userRepository.findByUsername(loginRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found for email: {}", loginRequestDto.getEmail());
                    return new BadRequestException("User not found");
                });

        log.info("Login successful for email: {}", loginRequestDto.getEmail());
        
        // Create auth response using enhanced mapper
        UserDetailsResponseDto userDetailsDto = userMapper.toEnhancedDto(userEntity);
        return new AuthResponseDto(token, userDetailsDto);
    }

    @Override
    @Transactional
    public UserDetailsResponseDto registerStudent(StudentRegisterRequestDto registerDto) {
        log.info("Registering new student with email: {}", registerDto.getEmail());
        validateUserRegistration(registerDto.getEmail());

        UserEntity student = new UserEntity();

        // Set common fields
        student.setUsername(registerDto.getEmail());
        student.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        student.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.ACTIVE);
        student.setEmail(registerDto.getEmail());

        // Set personal information
        student.setKhmerFirstName(registerDto.getKhmerFirstName());
        student.setKhmerLastName(registerDto.getKhmerLastName());
        student.setEnglishFirstName(registerDto.getEnglishFirstName());
        student.setEnglishLastName(registerDto.getEnglishLastName());
        student.setGender(registerDto.getGender());
        student.setDateOfBirth(registerDto.getDateOfBirth());
        student.setPhoneNumber(registerDto.getPhoneNumber());
        student.setCurrentAddress(registerDto.getCurrentAddress());
        student.setNationality(registerDto.getNationality());
        student.setEthnicity(registerDto.getEthnicity());
        student.setPlaceOfBirth(registerDto.getPlaceOfBirth());

        // Set student-specific fields
        student.setMemberSiblings(registerDto.getMemberSiblings());
        student.setNumberOfSiblings(registerDto.getNumberOfSiblings());

        // Assign to class if provided
        if (registerDto.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(registerDto.getClassId())
                    .orElseThrow(() -> new BadRequestException("Class not found with ID: " + registerDto.getClassId()));
            student.setClasses(classEntity);
        }

        // Set STUDENT role
        Role studentRole = roleRepository.findByName(RoleEnum.STUDENT)
                .orElseThrow(() -> new BadRequestException("Student role not found"));
        student.setRoles(Collections.singletonList(studentRole));

        UserEntity savedStudent = userRepository.save(student);
        log.info("Student registered successfully with ID: {}", savedStudent.getId());

        return userMapper.toEnhancedDto(savedStudent);
    }

    @Override
    @Transactional
    public UserDetailsResponseDto registerStaff(StaffRegisterRequestDto registerDto) {
        log.info("Registering new staff user with email: {}", registerDto.getEmail());
        validateUserRegistration(registerDto.getEmail());

        UserEntity staff = new UserEntity();

        // Set common fields
        staff.setUsername(registerDto.getEmail());
        staff.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        staff.setStatus(registerDto.getStatus() != null ? registerDto.getStatus() : Status.ACTIVE);
        staff.setEmail(registerDto.getEmail());

        // Set personal information
        staff.setKhmerFirstName(registerDto.getKhmerFirstName());
        staff.setKhmerLastName(registerDto.getKhmerLastName());
        staff.setEnglishFirstName(registerDto.getEnglishFirstName());
        staff.setEnglishLastName(registerDto.getEnglishLastName());
        staff.setGender(registerDto.getGender());
        staff.setDateOfBirth(registerDto.getDateOfBirth());
        staff.setPhoneNumber(registerDto.getPhoneNumber());
        staff.setCurrentAddress(registerDto.getCurrentAddress());
        staff.setNationality(registerDto.getNationality());
        staff.setEthnicity(registerDto.getEthnicity());
        staff.setPlaceOfBirth(registerDto.getPlaceOfBirth());

        // Set staff-specific fields
        staff.setStaffId(registerDto.getStaffId());
        staff.setNationalId(registerDto.getNationalId());
        staff.setIdentifyNumber(registerDto.getIdentifyNumber());
        staff.setStartWorkDate(registerDto.getStartWorkDate());
        staff.setCurrentPositionDate(registerDto.getCurrentPositionDate());
        staff.setEmployeeWork(registerDto.getEmployeeWork());
        staff.setDisability(registerDto.getDisability());
        staff.setPayroll_account_number(registerDto.getPayroll_account_number());
        staff.setCpp_membership_number(registerDto.getCpp_membership_number());
        staff.setProvince(registerDto.getProvince());
        staff.setDistrict(registerDto.getDistrict());
        staff.setCommune(registerDto.getCommune());
        staff.setVillage(registerDto.getVillage());
        staff.setOfficeName(registerDto.getOfficeName());
        staff.setCurrentPosition(registerDto.getCurrentPosition());
        staff.setDecreeFinal(registerDto.getDecreeFinal());
        staff.setRankAndClass(registerDto.getRankAndClass());

        // Assign to department if provided
        if (registerDto.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(registerDto.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found with ID: " + registerDto.getDepartmentId()));
            staff.setDepartment(department);
        }

        // Set roles
        List<Role> roles = new ArrayList<>();
        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            for (RoleEnum roleEnum : registerDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new BadRequestException("Invalid role: " + roleEnum));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName(RoleEnum.STAFF)
                    .orElseThrow(() -> new BadRequestException("Default role not found"));
            roles.add(defaultRole);
        }
        staff.setRoles(roles);

        UserEntity savedStaff = userRepository.save(staff);
        log.info("Staff user registered successfully with ID: {}", savedStaff.getId());

        return userMapper.toEnhancedDto(savedStaff);
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
            return new AuthResponseDto(newToken, userMapper.toEnhancedDto(userEntity));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BadRequestException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public UserDetailsResponseDto changePassword(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for current user");

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

        return userMapper.toEnhancedDto(updatedUser);
    }

    @Override
    public UserDetailsResponseDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
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

        return userMapper.toEnhancedDto(updatedUser);
    }
    
    private void validateUserRegistration(String email) {
        if (userRepository.existsByUsername(email)) {
            log.warn("Attempt to register with duplicate email: {}", email);
            throw new DuplicateNameException("Email is already in use");
        }
    }
}