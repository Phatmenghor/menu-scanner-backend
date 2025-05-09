package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.master.mapper.DepartmentMapper;
import com.menghor.ksit.feature.master.mapper.SubjectMapper;
import com.menghor.ksit.feature.school.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.school.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.school.dto.response.CourseResponseListDto;
import com.menghor.ksit.feature.school.dto.update.CourseUpdateDto;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {DepartmentMapper.class, SubjectMapper.class, StaffMapper.class}
)
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "user", ignore = true)
    CourseEntity toEntity(CourseRequestDto requestDto);

    @Mapping(target = "department", source = "department")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "teacher", source = "user")
    CourseResponseDto toResponseDto(CourseEntity courseEntity);

    // Method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(CourseUpdateDto dto, @MappingTarget CourseEntity entity);

    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "teacherName", expression = "java(getTeacherName(courseEntity))")
    CourseResponseListDto toList(CourseEntity courseEntity);

    default String getTeacherName(CourseEntity courseEntity) {
        if (courseEntity.getUser() == null) {
            return null;
        }

        // Combine names based on available data
        String firstName = courseEntity.getUser().getEnglishFirstName();
        String lastName = courseEntity.getUser().getEnglishLastName();

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return courseEntity.getUser().getUsername(); // Fallback to username
        }
    }

    default List<CourseResponseListDto> toResponseDtoList(List<CourseEntity> entities) {
        return entities.stream().map(this::toList).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<CourseResponseListDto> toCourseAllResponseDto(Page<CourseEntity> coursePage) {
        List<CourseResponseListDto> content = toResponseDtoList(coursePage.getContent());

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