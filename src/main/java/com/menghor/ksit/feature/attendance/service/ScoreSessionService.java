package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.util.List;

public interface ScoreSessionService {
    ScoreSessionResponseDto initializeScoreSession(ScoreSessionRequestDto requestDto);
    ScoreSessionResponseDto getScoreSessionById(Long id);
    ScoreSessionResponseDto updateScoreSession(ScoreSessionUpdateDto updateDto);
    CustomPaginationResponseDto<ScoreSessionResponseDto> getAllScoreSessions(ScoreSessionFilterDto filterDto);
}