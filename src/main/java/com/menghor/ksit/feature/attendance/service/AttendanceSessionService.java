package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.dto.response.QrResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceSessionService {
    AttendanceSessionDto findById(Long id);
    
    Page<AttendanceSessionDto> findAll(Long teacherId, Long scheduleId, Long classId,
                                       Boolean isFinal, Pageable pageable);
    
    List<AttendanceSessionDto> findByScheduleId(Long scheduleId);
    
    AttendanceSessionDto generateAttendanceSession(AttendanceSessionRequest request, Long teacherId);

    QrResponse generateQrCode(Long sessionId);

    AttendanceSessionDto markAttendanceByQr(QrAttendanceRequest request);
    
    AttendanceSessionDto finalizeAttendanceSession(Long sessionId);
}