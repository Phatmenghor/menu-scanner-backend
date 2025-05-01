package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.*;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class UserMapper {

    public abstract UserDto toDto(UserEntity user);

    // Implement custom toPageDto method to ensure pagination values are correct
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateUserFromDto(UserUpdateDto dto, @MappingTarget UserEntity entity);

}