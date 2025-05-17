package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceHistoryFilterDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import com.menghor.ksit.feature.attendance.service.AttendanceService;
import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AttendanceSessionService sessionService;

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDto> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }

    @PostMapping("/history")
    public ApiResponse<CustomPaginationResponseDto<AttendanceDto>> getAttendanceHistory(
            @RequestBody AttendanceHistoryFilterDto filterDto) {
        log.info("Fetching attendance history with filter: {}", filterDto);

        CustomPaginationResponseDto<AttendanceDto> response =
                attendanceService.findAttendanceHistory(filterDto);

        return new ApiResponse<>(
                "success",
                "Attendance history retrieved successfully",
                response
        );
    }
    
    @PutMapping("/update")
    public ResponseEntity<AttendanceDto> updateAttendance(@RequestBody AttendanceUpdateRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(request));
    }

}