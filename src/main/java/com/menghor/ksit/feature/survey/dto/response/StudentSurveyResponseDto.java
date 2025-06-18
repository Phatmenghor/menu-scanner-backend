package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class StudentSurveyResponseDto {
    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private String surveyDescription;
    private UserBasicInfoDto user;
    private LocalDateTime submittedAt;
    private Boolean isCompleted;

    // NEW COURSE INFORMATION FIELDS ⬇️
    private Long scheduleId;
    private String courseName;
    private String courseCode;
    private Integer credit;
    private Integer theory;
    private Integer execute;
    private Integer apply;
    private Integer totalHour;
    private String courseDescription;

    // NEW TEACHER INFORMATION FIELDS ⬇️
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;

    // NEW SCHEDULE INFORMATION FIELDS ⬇️
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot; // "08:00 - 10:00"
    private String roomName;
    private String className;

    // NEW SEMESTER INFORMATION FIELDS ⬇️
    private String semesterName; // "SEMESTER_1"
    private Integer academyYear;
    private String semesterDisplay; // "Semester 1, 2024"
    
    // Full survey structure as it was when submitted
    private List<SurveyResponseSectionDto> sections;
    
    private LocalDateTime createdAt;
}
