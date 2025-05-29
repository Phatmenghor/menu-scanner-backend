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

    // Main entity to response DTO mapping (detailed version with history)
    @Named("toDetailedResponseDto")
    @Mapping(target = "user", source = "user", qualifiedByName = "mapToUserBasicInfo")
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
    @Named("mapToUserBasicInfo")
    @Mapping(target = "userClass", source = "classes", qualifiedByName = "mapToClassBasicInfo")
    UserBasicInfoDto toUserBasicInfo(UserEntity user);

    // Class to Class Basic Info mapping
    @Named("mapToClassBasicInfo")
    ClassBasicInfoDto toClassBasicInfo(ClassEntity classEntity);

    // History entity to DTO mapping
    @Mapping(target = "user", source = "user", qualifiedByName = "mapToUserBasicInfoForHistory")
    RequestHistoryDto toHistoryDto(RequestHistoryEntity entity);

    // User to User Basic Info mapping for history (simpler version)
    @Named("mapToUserBasicInfoForHistory")
    @Mapping(target = "userClass", ignore = true)
    UserBasicInfoDto toUserBasicInfoForHistory(UserEntity user);

    // Summary/List view mapping (lighter version without full details)
    @Named("toListResponseDto")
    @Mapping(target = "user", source = "user", qualifiedByName = "mapToUserBasicInfo")
    RequestResponseDto toListResponseDto(RequestEntity entity);

    @Named("mapToHistoryDtoList")
    List<RequestHistoryDto> toHistoryDtoList(List<RequestHistoryEntity> entities);

    // List mappings - specify which method to use
    @IterableMapping(qualifiedByName = "toDetailedResponseDto")
    List<RequestResponseDto> toResponseDtoList(List<RequestEntity> entities);

    @IterableMapping(qualifiedByName = "toListResponseDto")
    List<RequestResponseDto> toListResponseDtoList(List<RequestEntity> entities);

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

    default CustomPaginationResponseDto<RequestResponseDto> toListPaginationResponse(Page<RequestEntity> page) {
        List<RequestResponseDto> content = toListResponseDtoList(page.getContent());

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
