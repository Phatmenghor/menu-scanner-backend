package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceHistoryFilterDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AttendanceService {
    AttendanceDto findById(Long id);
    
    Page<AttendanceDto> findAll(Long studentId, Long sessionId, AttendanceStatus status,
                                 Pageable pageable);

    AttendanceDto updateAttendance(AttendanceUpdateRequest request);

    List<AttendanceDto> findAllAttendanceHistory(AttendanceHistoryFilterDto filterDto);

    Long countAttendanceHistory(AttendanceHistoryFilterDto filterDto);

    CustomPaginationResponseDto<AttendanceDto> findAttendanceHistory(AttendanceHistoryFilterDto filterDto);
}