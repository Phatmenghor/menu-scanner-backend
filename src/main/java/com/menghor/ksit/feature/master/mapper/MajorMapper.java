package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.update.MajorUpdateDto;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {DepartmentMapper.class}
)
public interface MajorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "classes", ignore = true)
    MajorEntity toEntity(MajorRequestDto majorRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "status", source = "status")
    MajorResponseDto toResponseDto(MajorEntity majorEntity);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "classes", ignore = true)
    void updateEntityFromDto(MajorUpdateDto dto, @MappingTarget MajorEntity entity);


    default List<MajorResponseDto> toResponseDtoList(List<MajorEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<MajorResponseDto> toMajorAllResponseDto(Page<MajorEntity> majorPage) {
        List<MajorResponseDto> content = toResponseDtoList(majorPage.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                majorPage.getNumber() + 1,
                majorPage.getSize(),
                majorPage.getTotalElements(),
                majorPage.getTotalPages(),
                majorPage.isLast()
        );
    }
}