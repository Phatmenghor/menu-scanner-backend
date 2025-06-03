package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.school.dto.request.RequestCreateDto;
import com.menghor.ksit.feature.school.dto.response.ClassBasicInfoDto;
import com.menghor.ksit.feature.school.dto.response.RequestHistoryDto;
import com.menghor.ksit.feature.school.dto.response.RequestResponseDto;
import com.menghor.ksit.feature.school.dto.update.RequestUpdateDto;
import com.menghor.ksit.feature.school.model.RequestEntity;
import com.menghor.ksit.feature.school.model.RequestHistoryEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RequestMapper {

    // === BASIC MAPPINGS WITHOUT QUALIFIERS ===

    // Main entity to response DTO mapping
    RequestResponseDto toResponseDto(RequestEntity entity);

    // Create DTO to entity mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RequestEntity toEntity(RequestCreateDto createDto);

    // Update entity from DTO (only update non-null values)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(RequestUpdateDto updateDto, @MappingTarget RequestEntity entity);

    // User to User Basic Info mapping - Fixed duplicate userClass mapping
    @Mapping(target = "userClass", source = "classes", qualifiedByName = "mapUserClass")
    @Mapping(target = "departmentName", source = ".", qualifiedByName = "extractDepartmentName")
    @Mapping(target = "degree", source = "classes.degree", qualifiedByName = "mapDegreeToString")
    @Mapping(target = "majorName", source = ".", qualifiedByName = "extractMajor")
    UserBasicInfoDto toUserBasicInfo(UserEntity user);

    @Named("mapUserClass")
    default ClassBasicInfoDto mapUserClass(ClassEntity classEntity) {
        if (classEntity == null) return null;

        ClassBasicInfoDto dto = new ClassBasicInfoDto();
        dto.setId(classEntity.getId());
        dto.setCode(classEntity.getCode());
        dto.setCreatedAt(classEntity.getCreatedAt() != null ? classEntity.getCreatedAt().toString() : null);
        return dto;
    }

    @Named("mapDegreeToString")
    default String mapDegreeToString(com.menghor.ksit.enumations.DegreeEnum degree) {
        return degree != null ? degree.name() : null;
    }

    @Named("extractMajor")
    default String extractMajor(UserEntity user) {
        // Try to get major from different sources
        if (user.getClasses() != null && user.getClasses().getMajor() != null) {
            return user.getClasses().getMajor().getName();
        }
        if (user.getDepartment() != null) {
            return user.getDepartment().getName(); // Department name as major fallback
        }
        return null;
    }

    @Named("extractDepartmentName")
    default String extractDepartmentName(UserEntity user) {
        // Check if user is a student (has STUDENT role)
        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (isStudent) {
            // For students: get department from classes.major.department
            if (user.getClasses() != null &&
                    user.getClasses().getMajor() != null &&
                    user.getClasses().getMajor().getDepartment() != null) {
                return user.getClasses().getMajor().getDepartment().getName();
            }
        } else {
            // For other roles: get department directly from user.department
            if (user.getDepartment() != null) {
                return user.getDepartment().getName();
            }
        }

        return null;
    }

    // Named mapping methods to avoid ambiguity - these are the ONLY history mapping methods
    @Named("mapToBasicHistoryDto")
    @Mapping(target = "request.user", ignore = true)
    RequestHistoryDto mapToBasicHistoryDto(RequestHistoryEntity entity);

    @Named("mapToDetailedHistoryDto")
    @Mapping(target = "id", source = "id")  // Map the history entity's own ID
    @Mapping(target = "requestId", source = "request.id")  // Map request ID
    @Mapping(target = "requestCreatedAt", source = "request.createdAt")  // Map request creation date
    RequestHistoryDto mapToDetailedHistoryDto(RequestHistoryEntity entity);

    // List mappings - explicitly use the basic history mapping
    List<RequestResponseDto> toResponseDtoList(List<RequestEntity> entities);

    @IterableMapping(qualifiedByName = "mapToBasicHistoryDto")
    List<RequestHistoryDto> toHistoryDtoList(List<RequestHistoryEntity> entities);

    // List pagination response mapping (for lighter list views)
    default CustomPaginationResponseDto<RequestResponseDto> toListPaginationResponse(Page<RequestEntity> page) {
        List<RequestResponseDto> content = toResponseDtoList(page.getContent());

        return CustomPaginationResponseDto.<RequestResponseDto>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    default CustomPaginationResponseDto<RequestHistoryDto> toHistoryPaginationResponse(Page<RequestHistoryEntity> page) {
        List<RequestHistoryDto> content = toHistoryDtoList(page.getContent());

        return CustomPaginationResponseDto.<RequestHistoryDto>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}