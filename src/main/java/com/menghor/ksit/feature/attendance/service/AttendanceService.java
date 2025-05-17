package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {
    AttendanceDto findById(Long id);
    
    Page<AttendanceDto> findAll(Long studentId, Long sessionId, AttendanceStatus status,
                                 Pageable pageable);
    
    List<AttendanceDto> findBySessionId(Long sessionId);
    
    AttendanceDto updateAttendance(AttendanceUpdateRequest request);

    Double calculateAttendanceScore(Long studentId, Long scheduleId, LocalDateTime startDate, LocalDateTime endDate);
}