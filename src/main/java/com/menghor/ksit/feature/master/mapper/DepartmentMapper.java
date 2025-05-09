package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.update.DepartmentUpdateDto;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "majors", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "courses", ignore = true)
    DepartmentEntity toEntity(DepartmentRequestDto requestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "urlLogo", source = "urlLogo")
    DepartmentResponseDto toResponseDto(DepartmentEntity entity);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "majors", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "courses", ignore = true)
    void updateEntityFromDto(DepartmentUpdateDto dto, @MappingTarget DepartmentEntity entity);

    default List<DepartmentResponseDto> toResponseDtoList(List<DepartmentEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<DepartmentResponseDto> toDepartmentAllResponseDto(Page<DepartmentEntity> departmentPage) {
        List<DepartmentResponseDto> content = toResponseDtoList(departmentPage.getContent());
        return new CustomPaginationResponseDto<>(
                content,
                departmentPage.getNumber() + 1,
                departmentPage.getSize(),
                departmentPage.getTotalElements(),
                departmentPage.getTotalPages(),
                departmentPage.isLast()
        );
    }
}