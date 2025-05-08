package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.*;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.mapper.DepartmentMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ClassMapper.class, DepartmentMapper.class})
public abstract class UserMapper {

    @Autowired
    protected ClassMapper classMapper;

    @Autowired
    protected DepartmentMapper departmentMapper;

    /**
     * Convert UserEntity to UserDetailsResponseDto (Original method)
     */
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "department", ignore = true)
    public abstract UserDetailsResponseDto toEnhancedDto(UserEntity user);

    /**
     * Convert UserEntity to StaffUserResponseDto
     */
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "teachersProfessionalRank", source = "teachersProfessionalRank")
    @Mapping(target = "teacherExperience", source = "teacherExperience")
    @Mapping(target = "teacherPraiseOrCriticism", source = "teacherPraiseOrCriticism")
    @Mapping(target = "teacherEducation", source = "teacherEducation")
    @Mapping(target = "teacherVocational", source = "teacherVocational")
    @Mapping(target = "teacherShortCourse", source = "teacherShortCourse")
    @Mapping(target = "teacherLanguage", source = "teacherLanguage")
    @Mapping(target = "teacherFamily", source = "teacherFamily")
    public abstract StaffUserResponseDto toStaffUserDto(UserEntity user);

    /**
     * Convert UserEntity to StudentUserResponseDto
     */
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "studentStudiesHistory", source = "studentStudiesHistory")
    @Mapping(target = "studentParent", source = "studentParent")
    @Mapping(target = "studentSibling", source = "studentSibling")
    public abstract StudentUserResponseDto toStudentUserDto(UserEntity user);

    @AfterMapping
    protected void mapAdditionalFields(UserEntity user, @MappingTarget UserDetailsResponseDto.UserDetailsResponseDtoBuilder userDto) {
        if (user == null) {
            return;
        }

        // Extract roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        userDto.roles(userRoles);

        if (user.isStudent() && user.getClasses() != null) {
            userDto.studentClass(classMapper.toResponseDto(user.getClasses()));
        }

        if (user.isOther() && user.getDepartment() != null) {
            userDto.department(departmentMapper.toResponseDto(user.getDepartment()));
        }
    }

    @AfterMapping
    protected void mapStaffFields(UserEntity user, @MappingTarget StaffUserResponseDto.StaffUserResponseDtoBuilder staffDto) {
        if (user == null) {
            return;
        }

        // Extract roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        staffDto.roles(userRoles);

        if (user.getDepartment() != null) {
            staffDto.department(departmentMapper.toResponseDto(user.getDepartment()));
        }
    }

    @AfterMapping
    protected void mapStudentFields(UserEntity user, @MappingTarget StudentUserResponseDto.StudentUserResponseDtoBuilder studentDto) {
        if (user == null) {
            return;
        }

        if (user.getClasses() != null) {
            studentDto.studentClass(classMapper.toResponseDto(user.getClasses()));
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateUserFromDto(EnhancedUserUpdateDto dto, @MappingTarget UserEntity entity);

    @AfterMapping
    protected void updateConditionalFields(EnhancedUserUpdateDto dto, @MappingTarget UserEntity entity) {
        // No additional logic needed as conditional updates are handled by NullValuePropertyMappingStrategy.IGNORE
    }

    public abstract List<UserDetailsResponseDto> toEnhancedDtoList(List<UserEntity> entities);

    public abstract List<StaffUserResponseDto> toStaffUserDtoList(List<UserEntity> entities);

    public abstract List<StudentUserResponseDto> toStudentUserDtoList(List<UserEntity> entities);

    public UserAllResponseDto toPageResponse(List<UserDetailsResponseDto> content, Page<UserEntity> page) {
        UserAllResponseDto responseDto = new UserAllResponseDto();
        responseDto.setContent(content);
        responseDto.setPageNo(page.getNumber() + 1);
        responseDto.setPageSize(page.getSize());
        responseDto.setTotalElements(page.getTotalElements());
        responseDto.setTotalPages(page.getTotalPages());
        responseDto.setLast(page.isLast());
        return responseDto;
    }

    public StaffUserAllResponseDto toStaffPageResponse(List<StaffUserResponseDto> content, Page<UserEntity> page) {
        StaffUserAllResponseDto responseDto = new StaffUserAllResponseDto();
        responseDto.setContent(content);
        responseDto.setPageNo(page.getNumber() + 1);
        responseDto.setPageSize(page.getSize());
        responseDto.setTotalElements(page.getTotalElements());
        responseDto.setTotalPages(page.getTotalPages());
        responseDto.setLast(page.isLast());
        return responseDto;
    }

    public StudentUserAllResponseDto toStudentPageResponse(List<StudentUserResponseDto> content, Page<UserEntity> page) {
        StudentUserAllResponseDto responseDto = new StudentUserAllResponseDto();
        responseDto.setContent(content);
        responseDto.setPageNo(page.getNumber() + 1);
        responseDto.setPageSize(page.getSize());
        responseDto.setTotalElements(page.getTotalElements());
        responseDto.setTotalPages(page.getTotalPages());
        responseDto.setLast(page.isLast());
        return responseDto;
    }
}