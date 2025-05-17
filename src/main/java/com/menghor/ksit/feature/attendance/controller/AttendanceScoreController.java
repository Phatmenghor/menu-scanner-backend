package com.menghor.ksit.feature.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/attendance-scores")
@RequiredArgsConstructor
public class AttendanceScoreController {

    private final AttendanceScoreService scoreService;
    
    @GetMapping("/student")
    public ResponseEntity<AttendanceScoreDto> calculateForStudent(
            @RequestParam Long studentId,
            @RequestParam Long scheduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(scoreService.calculateForStudent(studentId, scheduleId, startDate, endDate));
        } else {
            return ResponseEntity.ok(scoreService.calculateForStudent(studentId, scheduleId));
        }
    }
    
    @GetMapping("/class")
    public ResponseEntity<List<AttendanceScoreDto>> calculateForClass(
            @RequestParam Long classId,
            @RequestParam Long scheduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(scoreService.calculateForClass(classId, scheduleId, startDate, endDate));
        } else {
            return ResponseEntity.ok(scoreService.calculateForClass(classId, scheduleId));
        }
    }
    
    @GetMapping("/course")
    public ResponseEntity<Page<AttendanceScoreDto>> calculateForCourse(
            @RequestParam Long courseId,
            @RequestParam Long semesterId,
            Pageable pageable) {
        
        return ResponseEntity.ok(scoreService.calculateForCourse(courseId, semesterId, pageable));
    }
}