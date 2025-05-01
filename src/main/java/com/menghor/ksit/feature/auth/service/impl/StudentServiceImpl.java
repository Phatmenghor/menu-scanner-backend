package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.service.StudentService;
import com.menghor.ksit.feature.auth.service.UserService;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    @Override
    public UserDetailsDto getStudentDetails(Long id) {
        log.info("Fetching student details for user ID: {}", id);

        UserEntity user = userRepository.findUserWithRolesById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            log.warn("User with ID {} is not a student", id);
            throw new BadRequestException("User is not a student");
        }

        log.info("Successfully retrieved student details for user ID: {}", id);
        return userService.getUserById(id);
    }

    @Override
    public UserDetailsDto getCurrentStudentDetails() {
        log.info("Fetching current student details");

        UserEntity currentUser = securityUtils.getCurrentUser();

        boolean isStudent = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            log.warn("Current user (ID: {}) is not a student", currentUser.getId());
            throw new BadRequestException("Current user is not a student");
        }

        log.info("Successfully retrieved current student details for user ID: {}", currentUser.getId());
        return userService.getUserByToken();
    }

    @Override
    public UserDetailsDto updateStudent(Long id, StudentUpdateDto updateDto) {
        log.info("Updating student with ID: {}", id);

        UserEntity student = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        boolean isStudent = student.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            log.warn("User with ID {} is not a student", id);
            throw new BadRequestException("User is not a student");
        }

        UserUpdateDto userUpdateDto = getUserUpdateDto(updateDto);
        log.info("Proceeding to update student info for user ID: {}", id);

        return userService.updateUser(id, userUpdateDto);
    }

    private static UserUpdateDto getUserUpdateDto(StudentUpdateDto updateDto) {
        return UserUpdateDto.builder()
                .username(updateDto.getUsername())
                .firstName(updateDto.getFirstName())
                .lastName(updateDto.getLastName())
                .contactNumber(updateDto.getContactNumber())
                .studentId(updateDto.getStudentId())
                .grade(updateDto.getGrade())
                .yearOfAdmission(updateDto.getYearOfAdmission())
                .status(updateDto.getStatus())
                .build();
    }
}
