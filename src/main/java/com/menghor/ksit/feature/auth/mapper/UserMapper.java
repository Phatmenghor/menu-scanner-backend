package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
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

    // Convert UserEntity to UserDetailsDto with roles
    public UserDetailsDto toDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        UserDetailsDto.UserDetailsDtoBuilder userDto = com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .contactNumber(user.getContactNumber());

        // Add student-specific fields
        userDto.studentId(user.getStudentId())
                .grade(user.getGrade())
                .yearOfAdmission(user.getYearOfAdmission());

        // Add staff/admin specific fields
        userDto.position(user.getPosition())
                .department(user.getDepartment())
                .employeeId(user.getEmployeeId());

        // Extract all roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        userDto.roles(userRoles);

        return userDto.build();
    }

    // Convert a page of entities to a response DTO
    public UserAllResponseDto toPageDto(List<UserDetailsDto> content, Page<UserEntity> userPage) {
        UserAllResponseDto responseDto = new UserAllResponseDto();
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
    public List<UserDetailsDto> toDtoList(List<UserEntity> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}