package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.mapper.RoomMapper;
import com.menghor.ksit.feature.master.mapper.SemesterMapper;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseListDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ClassMapper.class, StaffMapper.class, CourseMapper.class, RoomMapper.class, SemesterMapper.class}
)
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "classes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "semester", ignore = true)
    ScheduleEntity toEntity(ScheduleRequestDto requestDto);

    @Mapping(target = "classes", source = "classes")
    @Mapping(target = "teacher", source = "user")
    @Mapping(target = "course", source = "course")
    @Mapping(target = "room", source = "room")
    @Mapping(target = "yearLevel", source = "yearLevel")
    @Mapping(target = "semester", source = "semester")
    @Mapping(target = "surveyStatus", ignore = true)
    @Mapping(target = "surveySubmittedAt", ignore = true)
    @Mapping(target = "surveyResponseId", ignore = true)
    ScheduleResponseDto toResponseDto(ScheduleEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "classes", ignore = true)
    @Mapping(target = "user", ignore = true) add
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "semester", ignore = true)
    void updateEntityFromDto(ScheduleUpdateDto dto, @MappingTarget ScheduleEntity entity);

    default List<ScheduleResponseDto> toResponseDtoList(List<ScheduleEntity> entities) {
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    default CustomPaginationResponseDto<ScheduleResponseDto> toScheduleAllResponseDto(Page<ScheduleEntity> page) {
        List<ScheduleResponseDto> content = toResponseDtoList(page.getContent());

        return new CustomPaginationResponseDto<>(
                content,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}