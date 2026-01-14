package com.emenu.features.attendance.service;

import com.emenu.features.attendance.dto.filter.AttendanceFilterRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckInRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckOutRequest;
import com.emenu.features.attendance.dto.response.AttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceResponse checkIn(AttendanceCheckInRequest request);

    AttendanceResponse checkOut(UUID attendanceId, AttendanceCheckOutRequest request);

    AttendanceResponse getAttendanceById(UUID id);

    AttendanceResponse getTodayAttendance(UUID userId);

    List<AttendanceResponse> getAttendanceByUserId(Long userId, LocalDate startDate, LocalDate endDate);

    Page<AttendanceResponse> getAllAttendances(AttendanceFilterRequest filterRequest, Pageable pageable);
}
