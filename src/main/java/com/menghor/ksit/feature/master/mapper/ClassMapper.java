package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.classes.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseListDto;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {MajorMapper.class})
public interface ClassMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "major", ignore = true)
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

    @Mapping(target = "majorName", source = "major.name")
    ClassResponseListDto toList(ClassEntity classEntity);

    default List<ClassResponseListDto> toResponseDtoList(List<ClassEntity> entities) {
        return entities.stream().map(this::toList).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<ClassResponseListDto> toClassAllResponseDto(Page<ClassEntity> classPage) {
        List<ClassResponseListDto> content = toResponseDtoList(classPage.getContent());

        // Fix #1: Use a constructor instead of builder
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
