package com.menghor.ksit.feature.school.helper;

import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import org.springframework.stereotype.Component;

@Component
public class ScheduleFilterHelper {

    /**
     * Create a copy of filter with teacher ID constraint
     */
    public ScheduleFilterDto copyWithTeacherId(ScheduleFilterDto original, Long teacherId) {
        ScheduleFilterDto copy = new ScheduleFilterDto();
        copy.setSearch(original.getSearch());
        copy.setClassId(original.getClassId());
        copy.setRoomId(original.getRoomId());
        copy.setTeacherId(teacherId); // Override with specific teacher ID
        copy.setStudentId(null); // Clear studentId
        copy.setAcademyYear(original.getAcademyYear());
        copy.setSemester(original.getSemester());
        copy.setDayOfWeek(original.getDayOfWeek());
        copy.setStatus(original.getStatus());
        copy.setPageNo(original.getPageNo());
        copy.setPageSize(original.getPageSize());
        return copy;
    }

    /**
     * Create a copy of filter with class ID constraint
     */
    public ScheduleFilterDto copyWithClassId(ScheduleFilterDto original, Long classId) {
        ScheduleFilterDto copy = new ScheduleFilterDto();
        copy.setSearch(original.getSearch());
        copy.setClassId(classId); // Override with specific class ID
        copy.setRoomId(original.getRoomId());
        copy.setTeacherId(null); // Clear teacherId
        copy.setStudentId(null); // Clear studentId
        copy.setAcademyYear(original.getAcademyYear());
        copy.setSemester(original.getSemester());
        copy.setDayOfWeek(original.getDayOfWeek());
        copy.setStatus(original.getStatus());
        copy.setPageNo(original.getPageNo());
        copy.setPageSize(original.getPageSize());
        return copy;
    }

    /**
     * Create empty response helper
     */
    public com.menghor.ksit.utils.database.CustomPaginationResponseDto<com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto>
    createEmptyResponse(ScheduleFilterDto filterDto) {
        return com.menghor.ksit.utils.database.CustomPaginationResponseDto
                .<com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto>builder()
                .content(java.util.Collections.emptyList())
                .pageNo(filterDto.getPageNo() != null ? filterDto.getPageNo() : 1)
                .pageSize(filterDto.getPageSize() != null ? filterDto.getPageSize() : 10)
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .build();
    }
}