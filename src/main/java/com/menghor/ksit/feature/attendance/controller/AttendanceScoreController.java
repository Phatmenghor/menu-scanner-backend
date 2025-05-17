package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentAttendanceReportDto;
import com.menghor.ksit.feature.attendance.service.AttendanceScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-scores")
@RequiredArgsConstructor
public class AttendanceScoreController {
    private final AttendanceScoreService scoreService;

    @GetMapping("/class")
    public ResponseEntity<List<AttendanceScoreDto>> calculateForClass(
            @RequestParam Long classId,
            @RequestParam Long scheduleId) {

        return ResponseEntity.ok(scoreService.calculateForClass(classId, scheduleId));

    }

}