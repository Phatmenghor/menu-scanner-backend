package com.menghor.ksit.feature.master.mapper;

import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.subject.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.subject.response.SubjectResponseDto;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubjectEntity toEntity(SubjectRequestDto subjectRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    SubjectResponseDto toResponseDto(SubjectEntity subjectEntity);


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
