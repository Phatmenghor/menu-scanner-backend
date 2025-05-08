package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper interface for converting between User entities and Staff DTOs
 * Note: This interface is manually implemented in StaffMapperImpl
 */
public interface StaffMapper {

    /**
     * Convert UserEntity to StaffUserResponseDto
     */
    StaffUserResponseDto toStaffUserDto(UserEntity user);

    /**
     * Convert list of UserEntity to list of StaffUserResponseDto
     */
    List<StaffUserResponseDto> toStaffUserDtoList(List<UserEntity> entities);

    /**
     * Convert Page of UserEntity to StaffUserAllResponseDto
     */
    StaffUserAllResponseDto toStaffPageResponse(List<StaffUserResponseDto> content, Page<UserEntity> page);
}