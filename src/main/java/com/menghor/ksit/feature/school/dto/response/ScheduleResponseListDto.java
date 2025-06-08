package com.menghor.ksit.feature.school.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserListResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleResponseListDto {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;
    private Integer academyYear;
    private DayOfWeek day;
    private Status status;
    private Long classId;
    private StaffUserListResponseDto teacher;
    private Long courseId;
    private Long roomId;
    private Long semesterId;
    private LocalDateTime createdAt;
}