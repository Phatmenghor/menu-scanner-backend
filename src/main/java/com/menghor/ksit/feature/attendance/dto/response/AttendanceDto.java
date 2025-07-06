package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private AttendanceStatus status;
    private AttendanceType attendanceType;
    private String identifyNumber;
    private String comment;
    private LocalDateTime recordedTime;
    private AttendanceFinalizationStatus finalizationStatus;
    private Long studentId;
    private String studentName;
    private Long attendanceSessionId;

    // Teacher information
    private Long teacherId;
    private String teacherName;

    // Student personal information
    private GenderEnum gender;
    private LocalDate dateOfBirth;

    // Schedule and Course information
    private Long scheduleId;
    private String courseName;

    // Enhanced Course Details
    private String courseNameKH;
    private String courseNameEn;
    private String courseCode;
    private Integer credit;
    private Integer theory;
    private Integer execute;
    private Integer apply;
    private Integer totalHour;

    // Enhanced Schedule Details
    private LocalTime startTime;
    private LocalTime endTime;
    private DayOfWeek day;
    private YearLevelEnum yearLevel;

    // Room Information
    private Long roomId;
    private String roomName;

    // Class Information
    private Long classId;
    private String classCode;

    // Semester Information (Full Details)
    private Long semesterId;
    private SemesterEnum semester;
    private String semesterName;
    private Integer academyYear;

    private String createdAt;
}