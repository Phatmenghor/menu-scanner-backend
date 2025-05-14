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
    @Mapping(target = "semester", source = "semester")
    ScheduleResponseDto toResponseDto(ScheduleEntity entity);

    // New mapper for list response with IDs instead of full objects
    @Mapping(target = "classId", source = "classes.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "teacher", source = "user")
    @Mapping(target = "semesterId", source = "semester.id")
    ScheduleResponseListDto toResponseListDto(ScheduleEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "classes", ignore = true)
    @Mapping(target = "user", ignore = true)
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
