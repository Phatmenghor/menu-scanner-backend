package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.SemesterEnum;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleStudentsProgressDto {
    private Long scheduleId;
    private String courseName;
    private String courseCode;
    private String className;
    private String teacherName;
    private String roomName;
    private String dayOfWeek;

    // Separate time fields instead of combined timeSlot
    private LocalTime startTime;
    private LocalTime endTime;

    // Separate semester fields instead of combined semesterDisplay
    private SemesterEnum semester;
    private Integer academyYear;

    // Survey progress statistics
    private Integer totalStudents;
    private Integer completedSurveys;
    private Integer pendingSurveys;
    private Double completionPercentage;

    // List of all students with full info and survey status
    private List<ScheduleStudentSurveyDto> students;
}