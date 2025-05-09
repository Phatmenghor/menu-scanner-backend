package com.menghor.ksit.feature.course.mapper;

import com.menghor.ksit.feature.course.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseListDto;
import com.menghor.ksit.feature.course.model.CourseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "subject", ignore = true)
    CourseEntity toEntity(CourseRequestDto requestDto);

    @Mapping(target = "department", source = "department")
    @Mapping(target = "subject", source = "subject")
    CourseResponseDto toResponseDto(CourseEntity courseEntity);

    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "theory", source = "theory")
    @Mapping(target = "execute", source = "execute")
    @Mapping(target = "apply", source = "apply")
    CourseResponseListDto toList(CourseEntity courseEntity);

    default List<CourseResponseListDto> toResponseDtoList(List<CourseEntity> entities) {
        return entities.stream().map(this::toList).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<CourseResponseListDto> toCourseAllResponseDto(Page<CourseEntity> coursePage) {
        List<CourseResponseListDto> content = toResponseDtoList(coursePage.getContent());

        // Fix #1: Use a constructor instead of builder
        return new CustomPaginationResponseDto<>(
                content,
                coursePage.getNumber() + 1,
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages(),
                coursePage.isLast()
        );
    }
}
