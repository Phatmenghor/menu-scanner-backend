package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.department.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "majors", ignore = true)
    DepartmentEntity toEntity(DepartmentRequestDto requestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "url_logo", source = "url_logo")
    DepartmentResponseDto toResponseDto(DepartmentEntity entity);

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
