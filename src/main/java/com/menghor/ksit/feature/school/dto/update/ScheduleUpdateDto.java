package com.menghor.ksit.feature.school.dto.update;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleUpdateDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", example = "08:30")
    private LocalTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", example = "10:00")
    private LocalTime endTime;
    private DayOfWeek day;
    private Long classId;
    private Long teacherId;
    private Long courseId;
    private Long roomId;
    private Long semesterId;
    private Status status;
}