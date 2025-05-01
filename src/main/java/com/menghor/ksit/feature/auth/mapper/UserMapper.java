package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Component
public abstract class UserMapper {

    // Convert UserEntity to UserDto with roles
    public UserDto toDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNumber(user.getContactNumber());

        // Add student-specific fields if they exist
        if (user.getStudentId() != null) {
            userDto.studentId(user.getStudentId())
                    .grade(user.getGrade())
                    .yearOfAdmission(user.getYearOfAdmission());
        }

        // Add staff-specific fields if they exist
        if (user.getPosition() != null) {
            userDto.position(user.getPosition())
                    .department(user.getDepartment())
                    .employeeId(user.getEmployeeId());
        }

        // Extract all roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        userDto.roles(userRoles);

        // For backward compatibility - set primary role
        if (!userRoles.isEmpty()) {
            userDto.userRole(userRoles.get(0));
        }

        return userDto.build();
    }

    // Convert a page of entities to a response DTO
    public UserResponseDto toPageDto(List<UserDto> content, Page<UserEntity> userPage) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setContent(content);
        // Add 1 to page number to make it 1-based for clients
        responseDto.setPageNo(userPage.getNumber() + 1);
        responseDto.setPageSize(userPage.getSize());
        responseDto.setTotalElements(userPage.getTotalElements());
        responseDto.setTotalPages(userPage.getTotalPages());
        responseDto.setLast(userPage.isLast());
        return responseDto;
    }

    // Update user entity from DTO, ignoring null values
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateUserFromDto(UserUpdateDto dto, @MappingTarget UserEntity entity);

    // Convert a list of entities to DTOs
    public List<UserDto> toDtoList(List<UserEntity> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}