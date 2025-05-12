package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper interface for converting between User entities and Student DTOs
 * Note: This interface is manually implemented in StudentMapperImpl
 */
public interface StudentMapper {

    /**
     * Convert UserEntity to StudentUserResponseDto
     */
    StudentUserResponseDto toStudentUserDto(UserEntity user);


    StudentResponseDto toStudentBatchDto(UserEntity user, String plainTextPassword);

    /**
     * Convert list of UserEntity to list of StudentUserResponseDto
     */
    List<StudentUserResponseDto> toStudentUserDtoList(List<UserEntity> entities);

    /**
     * Convert Page of UserEntity to StudentUserAllResponseDto
     */
    StudentUserAllResponseDto toStudentPageResponse(List<StudentUserResponseDto> content, Page<UserEntity> page);
}