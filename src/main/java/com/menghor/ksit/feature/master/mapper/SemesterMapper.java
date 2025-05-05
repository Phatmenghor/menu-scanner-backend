package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.semester.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.semester.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SemesterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SemesterEntity toEntity(SemesterRequestDto requestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    SemesterResponseDto toResponseDto(SemesterEntity entity);

    default List<SemesterResponseDto> toResponseDtoList(List<SemesterEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<SemesterResponseDto> toSemesterAllResponseDto(Page<SemesterEntity> semesterPage) {
        List<SemesterResponseDto> content = toResponseDtoList(semesterPage.getContent());

        // Fix #1: Use a constructor instead of builder
        return new CustomPaginationResponseDto<>(
                content,
                semesterPage.getNumber() + 1,
                semesterPage.getSize(),
                semesterPage.getTotalElements(),
                semesterPage.getTotalPages(),
                semesterPage.isLast()
        );
    }
}
