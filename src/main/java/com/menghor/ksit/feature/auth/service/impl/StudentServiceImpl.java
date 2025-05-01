package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentDetailsDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
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
    public StudentDetailsDto getStudentDetails(Long id) {
        // Fetch user with roles
        UserEntity user = userRepository.findUserWithRolesById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Verify this is a student user
        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            throw new BadRequestException("User is not a student");
        }

        // Map to student details DTO
        return mapToStudentDetailsDto(user);
    }

    @Override
    public StudentDetailsDto getCurrentStudentDetails() {
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Verify this is a student user
        boolean isStudent = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            throw new BadRequestException("Current user is not a student");
        }

        // Map to student details DTO
        return mapToStudentDetailsDto(currentUser);
    }

    @Override
    public UserDto updateStudent(Long id, StudentUpdateDto updateDto) {
        // Fetch the student
        UserEntity student = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Verify this is a student user
        boolean isStudent = student.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (!isStudent) {
            throw new BadRequestException("User is not a student");
        }

        // Map student update DTO to user update DTO for the service
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setUsername(updateDto.getUsername());
        userUpdateDto.setFirstName(updateDto.getFirstName());
        userUpdateDto.setLastName(updateDto.getLastName());
        userUpdateDto.setContactNumber(updateDto.getContactNumber());
        userUpdateDto.setStudentId(updateDto.getStudentId());
        userUpdateDto.setGrade(updateDto.getGrade());
        userUpdateDto.setYearOfAdmission(updateDto.getYearOfAdmission());
        userUpdateDto.setStatus(updateDto.getStatus());

        // Use the user service to perform the update
        return userService.updateUser(id, userUpdateDto);
    }

    /**
     * Helper method to map UserEntity to StudentDetailsDto
     */
    private StudentDetailsDto mapToStudentDetailsDto(UserEntity user) {
        return StudentDetailsDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNumber(user.getContactNumber())
                .status(user.getStatus())
                .studentId(user.getStudentId())
                .grade(user.getGrade())
                .yearOfAdmission(user.getYearOfAdmission())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}