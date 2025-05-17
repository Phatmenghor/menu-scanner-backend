package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.response.AttendanceScoreDto;

import java.util.List;

public interface AttendanceScoreService {
    List<AttendanceScoreDto> calculateForClass(Long classId, Long scheduleId);
}