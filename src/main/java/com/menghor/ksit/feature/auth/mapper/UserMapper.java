package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
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

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "department", ignore = true)
    public abstract UserDetailsResponseDto toEnhancedDto(UserEntity user);

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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateUserFromDto(EnhancedUserUpdateDto dto, @MappingTarget UserEntity entity);

    @AfterMapping
    protected void updateConditionalFields(EnhancedUserUpdateDto dto, @MappingTarget UserEntity entity) {
        // No additional logic needed as conditional updates are handled by NullValuePropertyMappingStrategy.IGNORE
    }

    public abstract List<UserDetailsResponseDto> toEnhancedDtoList(List<UserEntity> entities);

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
}