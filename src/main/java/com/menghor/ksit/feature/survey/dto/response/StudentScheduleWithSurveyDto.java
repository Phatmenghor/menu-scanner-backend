package com.menghor.ksit.feature.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentScheduleWithSurveyDto {

    // Schedule Basic Info
    private Long scheduleId;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String teacherEmail;
    private String className;
    private String roomName;
    private String roomCode;

    // Schedule Timing
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // Survey Status
    private SurveyStatus surveyStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime surveySubmittedDate;

    private Long surveyResponseId;

    // Additional Schedule Info
    private String semester;
    private Integer academicYear;
    
    // Computed properties
    public String getTimeSlot() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return null;
    }
    
    public String getDayTimeDisplay() {
        return dayOfWeek + " " + getTimeSlot();
    }
}