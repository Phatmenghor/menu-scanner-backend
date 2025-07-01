package com.menghor.ksit.feature.school.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.enumations.YearLevelEnum;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserListResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "Schedule response with survey status information")
public class ScheduleResponseDto {

    @Schema(description = "Schedule ID")
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "Start time of the schedule", example = "08:00")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "End time of the schedule", example = "10:00")
    private LocalTime endTime;
    private YearLevelEnum yearLevel;
    private DayOfWeek day;
    private Status status;
    private ClassResponseDto classes;
    private StaffUserListResponseDto teacher;
    private CourseResponseMapWithScheduleDto course;
    private RoomResponseDto room;
    private SemesterResponseDto semester;
    private SurveyStatus surveyStatus;
    private LocalDateTime surveySubmittedAt;
    private Long surveyResponseId;
    private LocalDateTime createdAt;
}