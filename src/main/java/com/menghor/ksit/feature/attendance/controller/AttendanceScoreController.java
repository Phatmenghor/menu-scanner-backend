package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentAttendanceReportDto;
import com.menghor.ksit.feature.attendance.service.AttendanceScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class AttendanceScoreController {
    private final AttendanceScoreService scoreService;

    @GetMapping("/class")
    public ApiResponse<List<AttendanceScoreDto>> calculateForClass(
            @RequestParam Long classId,
            @RequestParam Long scheduleId) {
        log.info("Calculating attendance scores for class ID: {} and schedule ID: {}", classId, scheduleId);
        return new ApiResponse<>(
                "success",
                "Attendance scores calculated successfully",
                scoreService.calculateForClass(classId, scheduleId)
        );

    }

}