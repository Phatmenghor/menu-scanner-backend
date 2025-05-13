package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MajorMapper.class}
)
public interface ClassMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "major", ignore = true)
    @Mapping(target = "students", ignore = true)
    ClassEntity toEntity(ClassRequestDto classRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "academyYear", source = "academyYear")
    @Mapping(target = "degree", source = "degree")
    @Mapping(target = "yearLevel", source = "yearLevel")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "major", source = "major")
    @Mapping(target = "major.department", ignore = true)
    ClassResponseDto toResponseDto(ClassEntity classEntity);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "major", ignore = true)
    @Mapping(target = "students", ignore = true)
    void updateEntityFromDto(ClassUpdateDto dto, @MappingTarget ClassEntity entity);

    default List<ClassResponseDto> toResponseDtoList(List<ClassEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<ClassResponseDto> toClassAllResponseDto(Page<ClassEntity> classPage) {
        List<ClassResponseDto> content = toResponseDtoList(classPage.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                classPage.getNumber() + 1,
                classPage.getSize(),
                classPage.getTotalElements(),
                classPage.getTotalPages(),
                classPage.isLast()
        );
    }
}