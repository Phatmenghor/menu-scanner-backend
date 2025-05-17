package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentAttendanceReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceScoreService {
    AttendanceScoreDto calculateForStudent(Long studentId, Long scheduleId);
    AttendanceScoreDto calculateForStudent(Long studentId, Long scheduleId, LocalDateTime startDate, LocalDateTime endDate);
    List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId);
    List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId, LocalDateTime startDate, LocalDateTime endDate);
    Page<AttendanceScoreDto> calculateForCourse(Long courseId, Long semesterId, Pageable pageable);
    StudentAttendanceReportDto getStudentAttendanceReport(Long studentId, Long semesterId);
}