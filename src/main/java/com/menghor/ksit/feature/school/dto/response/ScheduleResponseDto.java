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

    @Schema(description = "Academic year")
    private Integer academyYear;

    private YearLevelEnum yearLevel;

    @Schema(description = "Day of the week")
    private DayOfWeek day;

    @Schema(description = "Schedule status")
    private Status status;

    @Schema(description = "Class information")
    private ClassResponseDto classes;

    @Schema(description = "Teacher information")
    private StaffUserListResponseDto teacher;

    @Schema(description = "Course information")
    private CourseResponseMapWithScheduleDto course;

    @Schema(description = "Room information")
    private RoomResponseDto room;

    @Schema(description = "Semester information")
    private SemesterResponseDto semester;

    // Survey related fields - CRITICAL for frontend alerts
    @Schema(description = "Survey completion status for current student",
            example = "NOT_STARTED or COMPLETED")
    private SurveyStatus surveyStatus;

    @Schema(description = "When the survey was submitted (only if completed)")
    private LocalDateTime surveySubmittedAt;

    @Schema(description = "Survey response ID (only if completed)")
    private Long surveyResponseId;

    @Schema(description = "Schedule creation timestamp")
    private LocalDateTime createdAt;
}