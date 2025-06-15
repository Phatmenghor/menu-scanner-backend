package com.menghor.ksit.feature.school.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleRequestDto {
    @NotNull(message = "Start time is required")
    @Schema(type = "string", pattern = "HH:mm", example = "08:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @Schema(type = "string", pattern = "HH:mm", example = "10:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;
    
    @NotNull(message = "Day of week is required")
    private DayOfWeek day;
    
    @NotNull(message = "Class ID is required")
    private Long classId;
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Semester ID is required")
    private Long semesterId;
    
    @NotNull(message = "Year level is required")
    private YearLevelEnum yearLevel;
    
    private Status status = Status.ACTIVE;
}