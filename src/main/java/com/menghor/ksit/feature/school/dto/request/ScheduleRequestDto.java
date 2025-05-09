package com.menghor.ksit.feature.school.dto.request;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleRequestDto {
    @NotBlank(message = "Start time is required")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    private String endTime;
    
    @NotNull(message = "Academy year is required")
    private Integer academyYear;
    
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
    
    private Status status = Status.ACTIVE;
}