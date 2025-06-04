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

    // User to User Basic Info mapping
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
        if (user.getClasses() != null && user.getClasses().getMajor() != null) {
            return user.getClasses().getMajor().getName();
        }
        if (user.getDepartment() != null) {
            return user.getDepartment().getName();
        }
        return null;
    }

    @Named("extractDepartmentName")
    default String extractDepartmentName(UserEntity user) {
        boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);

        if (isStudent) {
            if (user.getClasses() != null &&
                    user.getClasses().getMajor() != null &&
                    user.getClasses().getMajor().getDepartment() != null) {
                return user.getClasses().getMajor().getDepartment().getName();
            }
        } else {
            if (user.getDepartment() != null) {
                return user.getDepartment().getName();
            }
        }

        return null;
    }

    // UPDATED: History mapping methods with proper action user and request owner mapping

// Updated mapper methods for RequestMapper

    @Named("mapToBasicHistoryDto")
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "requestCreatedAt", source = "request.createdAt")
    @Mapping(target = "actionUser", source = "actionUser")
    @Mapping(target = "requestOwner", source = "request.user") // Get request owner through request.user
    RequestHistoryDto mapToBasicHistoryDto(RequestHistoryEntity entity);

    @Named("mapToDetailedHistoryDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "requestCreatedAt", source = "request.createdAt")
    @Mapping(target = "actionUser", source = "actionUser")
    @Mapping(target = "requestOwner", source = "request.user") // Get request owner through request.user
    RequestHistoryDto mapToDetailedHistoryDto(RequestHistoryEntity entity);

    // List mappings
    List<RequestResponseDto> toResponseDtoList(List<RequestEntity> entities);

    @IterableMapping(qualifiedByName = "mapToBasicHistoryDto")
    List<RequestHistoryDto> toHistoryDtoList(List<RequestHistoryEntity> entities);

    // Pagination response mappings
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