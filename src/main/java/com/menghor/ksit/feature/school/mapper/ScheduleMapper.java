package com.menghor.ksit.feature.school.mapper;

import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.mapper.RoomMapper;
import com.menghor.ksit.feature.master.mapper.SemesterMapper;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleBulkDuplicateResponseDto;
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
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "semester", ignore = true)
    void updateEntityFromDto(ScheduleUpdateDto dto, @MappingTarget ScheduleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "classes", ignore = true)
    @Mapping(target = "semester", ignore = true)
    @Mapping(target = "user", source = "sourceSchedule.user")
    @Mapping(target = "course", source = "sourceSchedule.course")
    @Mapping(target = "room", source = "sourceSchedule.room")
    @Mapping(target = "startTime", source = "sourceSchedule.startTime")
    @Mapping(target = "endTime", source = "sourceSchedule.endTime")
    @Mapping(target = "day", source = "sourceSchedule.day")
    @Mapping(target = "yearLevel", source = "sourceSchedule.yearLevel")
    @Mapping(target = "status", constant = "ACTIVE")
    ScheduleEntity duplicateSchedule(ScheduleEntity sourceSchedule);

    // Create bulk duplicate response
    @Mapping(target = "sourceClassName", source = "sourceClass.code")
    @Mapping(target = "sourceSemesterName", source = "sourceSemester.semester")
    @Mapping(target = "sourceSemesterYear", source = "sourceSemester.academyYear")
    @Mapping(target = "targetClassName", source = "targetClass.code")
    @Mapping(target = "targetSemesterName", source = "targetSemester.semester")
    @Mapping(target = "targetSemesterYear", source = "targetSemester.academyYear")
    @Mapping(target = "duplicatedSchedules", ignore = true)
    @Mapping(target = "errors", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "totalSourceSchedules", ignore = true)
    @Mapping(target = "successfullyDuplicated", ignore = true)
    @Mapping(target = "skipped", ignore = true)
    @Mapping(target = "failed", ignore = true)
    ScheduleBulkDuplicateResponseDto toBulkDuplicateResponse(
            Long sourceClassId, ClassEntity sourceClass, Long sourceSemesterId, SemesterEntity sourceSemester,
            Long targetClassId, ClassEntity targetClass, Long targetSemesterId, SemesterEntity targetSemester
    );

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