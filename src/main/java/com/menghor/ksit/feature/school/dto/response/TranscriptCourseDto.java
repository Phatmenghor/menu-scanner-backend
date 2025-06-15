package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.CourseStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TranscriptCourseDto {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseNameKH;

    // Course details
    private Integer credit; // Single credit field
    private Integer theory;  // Theory hours
    private Integer execute; // Execute/practical hours
    private Integer apply;   // Applied hours
    private Integer totalHour; // Total hours

    // Schedule information
    private Long scheduleId;
    private String dayOfWeek;
    private String timeSlot;
    private String roomName;
    private String teacherName;

    // Score information
    private BigDecimal totalScore; // Out of 100
    private String letterGrade; // A, B, C, D, F or "---" for in progress
    private BigDecimal gradePoints; // For GPA calculation
    private CourseStatusEnum status; // COMPLETED, IN_PROGRESS

    // Score breakdown
    private BigDecimal attendanceScore;
    private BigDecimal assignmentScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;
}