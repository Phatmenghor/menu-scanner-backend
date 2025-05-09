package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.response.SubjectResponseDto;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SubjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "courses", ignore = true)
    SubjectEntity toEntity(SubjectRequestDto subjectRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    SubjectResponseDto toResponseDto(SubjectEntity subjectEntity);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "courses", ignore = true)
    void updateEntityFromDto(SubjectRequestDto dto, @MappingTarget SubjectEntity entity);

    default List<SubjectResponseDto> toSubjectDtoList(List<SubjectEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<SubjectResponseDto> toSubjectAllResponseDto(Page<SubjectEntity> subjectPage) {
        List<SubjectResponseDto> content = toSubjectDtoList(subjectPage.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                subjectPage.getNumber() + 1,
                subjectPage.getSize(),
                subjectPage.getTotalElements(),
                subjectPage.getTotalPages(),
                subjectPage.isLast()
        );
    }
}
