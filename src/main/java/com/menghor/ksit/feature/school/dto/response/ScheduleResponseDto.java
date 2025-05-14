package com.menghor.ksit.feature.school.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserListResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleResponseDto {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;
    private Integer academyYear;
    private DayOfWeek day;
    private Status status;
    private ClassResponseDto classes;
    private StaffUserListResponseDto teacher;
    private CourseResponseMapWithScheduleDto course;
    private RoomResponseDto room;
    private SemesterResponseDto semester;
    private LocalDateTime createdAt;
}

