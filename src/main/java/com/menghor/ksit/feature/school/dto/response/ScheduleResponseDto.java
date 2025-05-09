package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleResponseDto {
    private Long id;
    private String startTime;
    private String endTime;
    private Integer academyYear;
    private DayOfWeek day;
    private Status status;
    private ClassResponseDto classes;
    private StaffUserResponseDto teacher;
    private CourseResponseDto course;
    private RoomResponseDto room;
    private SemesterResponseDto semester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}