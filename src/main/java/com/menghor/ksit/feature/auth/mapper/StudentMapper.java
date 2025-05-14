package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserListResponseDto;
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

    StudentUserResponseDto toStudentUserDto(UserEntity user);

    StudentUserListResponseDto toStudentListUserDto(UserEntity user);

    StudentResponseDto toStudentBatchDto(UserEntity user, String plainTextPassword);

    List<StudentUserListResponseDto> toStudentUserDtoList(List<UserEntity> entities);

    StudentUserAllResponseDto toStudentPageResponse(List<StudentUserListResponseDto> content, Page<UserEntity> page);
}