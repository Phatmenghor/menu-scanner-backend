package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/score-sessions")
@RequiredArgsConstructor
@Slf4j
public class ScoreSessionController {
    
    private final ScoreSessionService scoreSessionService;
    private final SecurityUtils securityUtils;
    
    @PostMapping("/initialize")
    public ApiResponse<ScoreSessionResponseDto> initializeScoreSession(@Valid @RequestBody ScoreSessionRequestDto requestDto) {
        log.info("REST request to initialize score session: {}", requestDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.initializeScoreSession(requestDto);
        return new ApiResponse<>(
                "success",
                "Score session initialized successfully",
                responseDto
        );
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ScoreSessionResponseDto> getScoreSessionById(@PathVariable Long id) {
        log.info("REST request to get score session by ID: {}", id);
        ScoreSessionResponseDto responseDto = scoreSessionService.getScoreSessionById(id);
        return new ApiResponse<>(
                "success",
                "Score session retrieved successfully",
                responseDto
        );
    }

    @PutMapping("/update")
    public ApiResponse<ScoreSessionResponseDto> updateScoreSession(
            @Valid @RequestBody ScoreSessionUpdateDto updateDto) {
        log.info("REST request to update score session: {}", updateDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.updateScoreSession(updateDto);
        return new ApiResponse<>(
                "success",
                "Score session updated successfully",
                responseDto
        );
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<ScoreSessionResponseDto>> getAllScoreSessions(
            @Valid @RequestBody ScoreSessionFilterDto filterDto) {
        log.info("REST request to get all score sessions with filter: {}", filterDto);

        CustomPaginationResponseDto<ScoreSessionResponseDto> response =
                scoreSessionService.getAllScoreSessions(filterDto);

        return new ApiResponse<>(
                "success",
                "Score sessions retrieved successfully",
                response
        );
    }
}