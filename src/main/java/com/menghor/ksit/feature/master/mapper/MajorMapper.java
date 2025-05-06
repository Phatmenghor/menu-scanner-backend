package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.major.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseListDto;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class})
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

    @Mapping(target = "departmentName", source = "department.name")
    MajorResponseListDto toList(MajorEntity majorEntity);

    default List<MajorResponseListDto> toResponseDtoList(List<MajorEntity> entities) {
        return entities.stream().map(this::toList).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<MajorResponseListDto> toMajorAllResponseDto(Page<MajorEntity> majorPage) {
        List<MajorResponseListDto> content = toResponseDtoList(majorPage.getContent());

        // Fix #1: Use a constructor instead of builder
        return new CustomPaginationResponseDto<>(
                content,
                majorPage.getNumber() + 1,
                majorPage.getSize(),
                majorPage.getTotalElements(),
                majorPage.getTotalPages(),
                majorPage.isLast()
        );
    }

//    default List<MajorResponseDto> toResponseDtoList(List<MajorEntity> entities) {
//        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
//    }
//
//    default CustomPaginationResponseDto<MajorResponseDto> toMajorAllResponseDto(Page<MajorEntity> majorPage) {
//        List<MajorResponseDto> content = toResponseDtoList(majorPage.getContent());
//
//        // Fix #1: Use a constructor instead of builder
//        return new CustomPaginationResponseDto<>(
//                content,
//                majorPage.getNumber() + 1,
//                majorPage.getSize(),
//                majorPage.getTotalElements(),
//                majorPage.getTotalPages(),
//                majorPage.isLast()
//        );
//    }
}
