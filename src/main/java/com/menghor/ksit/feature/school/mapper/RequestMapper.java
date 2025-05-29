package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.school.dto.request.RequestCreateDto;
import com.menghor.ksit.feature.school.dto.request.RequestHistoryDto;
import com.menghor.ksit.feature.school.dto.response.ClassBasicInfoDto;
import com.menghor.ksit.feature.school.dto.response.RequestResponseDto;
import com.menghor.ksit.feature.school.dto.response.UserBasicInfoDto;
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
    @Mapping(target = "staffComment", ignore = true)
    RequestEntity toEntity(RequestCreateDto createDto);

    // Update entity from DTO (only update non-null values)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "staffComment", ignore = true)
    void updateEntityFromDto(RequestUpdateDto updateDto, @MappingTarget RequestEntity entity);

    // User to User Basic Info mapping
    @Mapping(target = "userClass", ignore = true)  // Ignore until we fix the field name
    UserBasicInfoDto toUserBasicInfo(UserEntity user);

    // Class to Class Basic Info mapping
    ClassBasicInfoDto toClassBasicInfo(ClassEntity classEntity);

    // History entity to DTO mapping
    RequestHistoryDto toHistoryDto(RequestHistoryEntity entity);

    // List mappings
    List<RequestResponseDto> toResponseDtoList(List<RequestEntity> entities);
    List<RequestHistoryDto> toHistoryDtoList(List<RequestHistoryEntity> entities);

    // Pagination response mapping
    default CustomPaginationResponseDto<RequestResponseDto> toPaginationResponse(Page<RequestEntity> page) {
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
}