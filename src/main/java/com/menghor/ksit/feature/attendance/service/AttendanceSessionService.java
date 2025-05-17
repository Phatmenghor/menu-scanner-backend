package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.request.AttendanceSessionRequest;
import com.menghor.ksit.feature.attendance.dto.request.QrAttendanceRequest;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceSessionDto;
import com.menghor.ksit.feature.attendance.dto.response.QrResponse;

public interface AttendanceSessionService {
    AttendanceSessionDto findById(Long id);

    AttendanceSessionDto generateAttendanceSession(AttendanceSessionRequest request, Long teacherId);

    QrResponse regenerateQrCode(Long sessionId);

    AttendanceSessionDto markAttendanceByQr(QrAttendanceRequest request);
    
    AttendanceSessionDto finalizeAttendanceSession(Long sessionId);
}